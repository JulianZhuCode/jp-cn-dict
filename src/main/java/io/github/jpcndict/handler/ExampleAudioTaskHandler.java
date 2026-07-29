package io.github.jpcndict.handler;

import io.github.jpcndict.entity.ExampleEntity;
import io.github.jpcndict.repository.ExampleRepository;
import io.github.jpcndict.service.ExampleService;
import io.github.springwhale.task.handler.TaskHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Task handler for batch example audio regeneration.
 * <p>
 * Task type: {@code EXAMPLE_AUDIO}
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

    private final ExampleService exampleService;
    private final ExampleRepository exampleRepository;

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
    public void beforeStart(Map<String, Object> params) {
        log.info("Starting EXAMPLE_AUDIO task, total examples={}", exampleRepository.count());
    }

    @Override
    public void afterComplete(Map<String, Object> params) {
        log.info("EXAMPLE_AUDIO task completed");
    }
}