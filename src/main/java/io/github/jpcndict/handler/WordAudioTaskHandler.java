package io.github.jpcndict.handler;

import io.github.jpcndict.entity.WordEntity;
import io.github.jpcndict.repository.WordRepository;
import io.github.jpcndict.service.AudioService;
import io.github.jpcndict.service.WordService;
import io.github.springwhale.task.handler.TaskHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Task handler for batch word audio regeneration.
 * <p>
 * Task type: {@code WORD_AUDIO}
 * <p>
 * Uses batch audio generation via {@link AudioService#batchGenerateWordAudio(List, List)}
 * which internally leverages {@link io.github.springwhale.framework.core.utils.EdgeTtsUtil}
 * thread pool for concurrent TTS processing.
 * <p>
 * Optional params:
 * <ul>
 *   <li>{@code ids} - List of specific word IDs to regenerate</li>
 *   <li>{@code onlyMissing} - If true, only regenerate words without audioUrl</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WordAudioTaskHandler implements TaskHandler {

    private final WordService wordService;
    private final WordRepository wordRepository;
    private final AudioService audioService;

    @Override
    public String getTaskType() {
        return "WORD_AUDIO";
    }

    @Override
    public List<String> enumerateItems(Map<String, Object> params) {
        List<?> ids = params != null ? (List<?>) params.get("ids") : null;
        Boolean onlyMissing = params != null ? (Boolean) params.get("onlyMissing") : null;

        if (ids != null && !ids.isEmpty()) {
            return ids.stream()
                    .map(id -> "word:" + id)
                    .toList();
        }

        List<WordEntity> words;
        if (Boolean.TRUE.equals(onlyMissing)) {
            words = wordRepository.findAll().stream()
                    .filter(w -> !StringUtils.hasText(w.getAudioUrl()))
                    .toList();
        } else {
            words = wordRepository.findAll();
        }

        return words.stream()
                .map(w -> "word:" + w.getId())
                .toList();
    }

    @Override
    public boolean processItem(String itemKey, Map<String, Object> params) {
        int wordId = Integer.parseInt(itemKey.split(":")[1]);
        return wordService.regenerateAudio(wordId);
    }

    private static final int CHUNK_SIZE = 100;

    @Override
    public void processBatch(List<String> itemKeys, Map<String, Object> params,
                             BatchProgressCallback callback) throws Exception {
        if (itemKeys.isEmpty()) {
            log.info("WordAudioTaskHandler.processBatch: empty itemKeys, skipping");
            return;
        }

        log.info("WordAudioTaskHandler.processBatch: starting batch of {} items, chunkSize={}",
                itemKeys.size(), CHUNK_SIZE);

        List<Integer> allWordIds = itemKeys.stream()
                .map(key -> Integer.parseInt(key.split(":")[1]))
                .toList();

        List<WordEntity> allWords = wordRepository.findAllById(allWordIds);
        Map<Integer, WordEntity> wordMap = new HashMap<>();
        for (WordEntity w : allWords) {
            wordMap.put(w.getId(), w);
        }

        List<WordEntity> pendingSave = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger totalSuccess = new AtomicInteger(0);
        AtomicInteger totalFail = new AtomicInteger(0);

        int totalChunks = (itemKeys.size() + CHUNK_SIZE - 1) / CHUNK_SIZE;
        int chunkIndex = 0;

        for (int offset = 0; offset < itemKeys.size(); offset += CHUNK_SIZE) {
            if (callback.isCancelled()) {
                log.info("WordAudioTaskHandler: cancelled before chunk {}/{}", chunkIndex + 1, totalChunks);
                break;
            }

            chunkIndex++;
            int end = Math.min(offset + CHUNK_SIZE, itemKeys.size());
            List<Integer> chunkIds = allWordIds.subList(offset, end);

            List<String> chunkTexts = new ArrayList<>(chunkIds.size());
            for (Integer id : chunkIds) {
                WordEntity w = wordMap.get(id);
                chunkTexts.add(w != null ? w.getWord() : "");
            }

            log.info("WordAudioTaskHandler: processing chunk {}/{} (items {}-{})",
                    chunkIndex, totalChunks, offset, end - 1);
            long chunkStart = System.currentTimeMillis();

            AtomicInteger chunkSuccess = new AtomicInteger(0);
            AtomicInteger chunkFail = new AtomicInteger(0);

            audioService.batchGenerateWordAudio(chunkIds, chunkTexts, (id, success, url, errorMsg) -> {
                if (callback.isCancelled()) {
                    return;
                }

                WordEntity word = wordMap.get(id);
                String itemKey = "word:" + id;

                if (word != null) {
                    if (success && url != null) {
                        word.setAudioUrl(url);
                        pendingSave.add(word);
                        callback.onItemResult(itemKey, true);
                        chunkSuccess.incrementAndGet();
                        totalSuccess.incrementAndGet();
                    } else {
                        String errMsg = errorMsg != null ? errorMsg : "音频生成失败";
                        callback.onItemResult(itemKey, false, errMsg);
                        chunkFail.incrementAndGet();
                        totalFail.incrementAndGet();
                    }
                } else {
                    callback.onItemResult(itemKey, false, "条目不存在: word:" + id);
                    chunkFail.incrementAndGet();
                    totalFail.incrementAndGet();
                }

                synchronized (pendingSave) {
                    if (pendingSave.size() >= 50) {
                        List<WordEntity> batch = new ArrayList<>(pendingSave);
                        wordRepository.saveAll(batch);
                        pendingSave.clear();
                    }
                }
            });

            synchronized (pendingSave) {
                if (!pendingSave.isEmpty()) {
                    wordRepository.saveAll(pendingSave);
                    pendingSave.clear();
                }
            }

            callback.flush();

            long chunkElapsed = System.currentTimeMillis() - chunkStart;
            log.info("WordAudioTaskHandler: chunk {}/{} completed in {}ms, success={}, fail={}",
                    chunkIndex, totalChunks, chunkElapsed, chunkSuccess.get(), chunkFail.get());
        }

        log.info("WordAudioTaskHandler.processBatch: all chunks done, total success={}, fail={}",
                totalSuccess.get(), totalFail.get());
    }

    @Override
    public void beforeStart(Map<String, Object> params) {
        log.info("Starting WORD_AUDIO task, total words={}", wordRepository.count());
    }

    @Override
    public void afterComplete(Map<String, Object> params) {
        log.info("WORD_AUDIO task completed");
    }
}