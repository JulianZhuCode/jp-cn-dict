package io.github.jpcndict.handler;

import io.github.jpcndict.entity.WordEntity;
import io.github.jpcndict.repository.WordRepository;
import io.github.jpcndict.service.WordService;
import io.github.springwhale.task.handler.TaskHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Task handler for batch word audio regeneration.
 * <p>
 * Task type: {@code WORD_AUDIO}
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

    @Override
    public void beforeStart(Map<String, Object> params) {
        log.info("Starting WORD_AUDIO task, total words={}", wordRepository.count());
    }

    @Override
    public void afterComplete(Map<String, Object> params) {
        log.info("WORD_AUDIO task completed");
    }
}