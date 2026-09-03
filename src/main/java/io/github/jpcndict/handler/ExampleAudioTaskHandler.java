package io.github.jpcndict.handler;

import io.github.jpcndict.dao.entity.ExampleEntity;
import io.github.jpcndict.dao.repository.ExampleRepository;
import io.github.jpcndict.service.AudioService;
import io.github.jpcndict.service.ExampleService;
import io.github.springwhale.platform.task.handler.TaskHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Task handler for batch example audio regeneration.
 * <p>
 * Task type: {@code EXAMPLE_AUDIO}
 * <p>
 * Uses batch audio generation via {@link AudioService#batchGenerateExampleAudio(List, List)}
 * which internally leverages {@link io.github.springwhale.framework.core.utils.EdgeTtsUtil}
 * thread pool for concurrent TTS processing.
 * <p>
 * Optional params:
 * <ul>
 *   <li>{@code ids} - List of specific example IDs to regenerate</li>
 *   <li>{@code onlyMissing} - If true, only regenerate examples without audioUrl</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExampleAudioTaskHandler implements TaskHandler {

    private static final int CHUNK_SIZE = 100;
    private final ExampleService exampleService;
    private final ExampleRepository exampleRepository;
    private final AudioService audioService;

    @Override
    public String getTaskType() {
        return "EXAMPLE_AUDIO";
    }

    @Override
    public List<String> enumerateItems(Map<String, Object> params) {
        List<?> ids = params != null ? (List<?>) params.get("ids") : null;
        Boolean onlyMissing = params != null ? (Boolean) params.get("onlyMissing") : null;

        if (ids != null && !ids.isEmpty()) {
            return ids.stream()
                    .map(id -> "example:" + id)
                    .toList();
        }

        List<ExampleEntity> examples;
        if (Boolean.TRUE.equals(onlyMissing)) {
            examples = exampleRepository.findAll().stream()
                    .filter(e -> !StringUtils.hasText(e.getAudioUrl()))
                    .toList();
        } else {
            examples = exampleRepository.findAll();
        }

        return examples.stream()
                .map(e -> "example:" + e.getId())
                .toList();
    }

    @Override
    public boolean processItem(String itemKey, Map<String, Object> params) {
        int exampleId = Integer.parseInt(itemKey.split(":")[1]);
        return exampleService.regenerateAudio(exampleId);
    }

    @Override
    public void processBatch(List<String> itemKeys, Map<String, Object> params,
                             BatchProgressCallback callback) throws Exception {
        if (itemKeys.isEmpty()) {
            log.info("ExampleAudioTaskHandler.processBatch: empty itemKeys, skipping");
            return;
        }

        log.info("ExampleAudioTaskHandler.processBatch: starting batch of {} items, chunkSize={}",
                itemKeys.size(), CHUNK_SIZE);

        List<Integer> allExampleIds = itemKeys.stream()
                .map(key -> Integer.parseInt(key.split(":")[1]))
                .toList();

        List<ExampleEntity> allExamples = exampleRepository.findAllById(allExampleIds);
        Map<Integer, ExampleEntity> exampleMap = new HashMap<>();
        for (ExampleEntity e : allExamples) {
            exampleMap.put(e.getId(), e);
        }

        List<ExampleEntity> pendingSave = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger totalSuccess = new AtomicInteger(0);
        AtomicInteger totalFail = new AtomicInteger(0);

        int totalChunks = (itemKeys.size() + CHUNK_SIZE - 1) / CHUNK_SIZE;
        int chunkIndex = 0;

        for (int offset = 0; offset < itemKeys.size(); offset += CHUNK_SIZE) {
            if (callback.isCancelled()) {
                log.info("ExampleAudioTaskHandler: cancelled before chunk {}/{}", chunkIndex + 1, totalChunks);
                break;
            }

            chunkIndex++;
            int end = Math.min(offset + CHUNK_SIZE, itemKeys.size());
            List<Integer> chunkIds = allExampleIds.subList(offset, end);

            List<String> chunkTexts = new ArrayList<>(chunkIds.size());
            for (Integer id : chunkIds) {
                ExampleEntity e = exampleMap.get(id);
                chunkTexts.add(e != null ? e.getJp() : "");
            }

            log.info("ExampleAudioTaskHandler: processing chunk {}/{} (items {}-{})",
                    chunkIndex, totalChunks, offset, end - 1);
            long chunkStart = System.currentTimeMillis();

            AtomicInteger chunkSuccess = new AtomicInteger(0);
            AtomicInteger chunkFail = new AtomicInteger(0);

            audioService.batchGenerateExampleAudio(chunkIds, chunkTexts, (id, success, url, errorMsg) -> {
                if (callback.isCancelled()) {
                    return;
                }

                ExampleEntity example = exampleMap.get(id);
                String itemKey = "example:" + id;

                if (example != null) {
                    if (success && url != null) {
                        example.setAudioUrl(url);
                        pendingSave.add(example);
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
                    callback.onItemResult(itemKey, false, "条目不存在: example:" + id);
                    chunkFail.incrementAndGet();
                    totalFail.incrementAndGet();
                }

                synchronized (pendingSave) {
                    if (pendingSave.size() >= 50) {
                        List<ExampleEntity> batch = new ArrayList<>(pendingSave);
                        exampleRepository.saveAll(batch);
                        pendingSave.clear();
                    }
                }
            });

            synchronized (pendingSave) {
                if (!pendingSave.isEmpty()) {
                    exampleRepository.saveAll(pendingSave);
                    pendingSave.clear();
                }
            }

            callback.flush();

            long chunkElapsed = System.currentTimeMillis() - chunkStart;
            log.info("ExampleAudioTaskHandler: chunk {}/{} completed in {}ms, success={}, fail={}",
                    chunkIndex, totalChunks, chunkElapsed, chunkSuccess.get(), chunkFail.get());
        }

        log.info("ExampleAudioTaskHandler.processBatch: all chunks done, total success={}, fail={}",
                totalSuccess.get(), totalFail.get());
    }

    @Override
    public void beforeStart(Map<String, Object> params) {
        log.info("Starting EXAMPLE_AUDIO task, total examples={}", exampleRepository.count());
    }

    @Override
    public void afterComplete(Map<String, Object> params) {
        log.info("EXAMPLE_AUDIO task completed");
    }
}