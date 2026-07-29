package io.github.jpcndict.controller;

import io.github.jpcndict.dto.request.AiAnalyzeRequest;
import io.github.jpcndict.dto.request.ExampleRequest;
import io.github.jpcndict.dto.vo.AiAnalyzeResult;
import io.github.jpcndict.dto.vo.ExampleVO;
import io.github.jpcndict.service.ExampleAiService;
import io.github.jpcndict.service.ExampleService;
import io.github.springwhale.framework.webmvc.advice.AdviceIgnore;
import io.github.springwhale.task.dto.request.TaskCreateRequest;
import io.github.springwhale.task.dto.vo.TaskVO;
import io.github.springwhale.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/examples")
@RequiredArgsConstructor
public class ExampleController {

    private final ExampleService exampleService;
    private final ExampleAiService exampleAiService;
    private final TaskService taskService;

    /**
     * 分页查询所有例句（支持筛选）
     * GET /api/examples?page=0&size=20&keyword=
     */
    @GetMapping
    public Page<ExampleVO> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        return exampleService.search(keyword, pageable);
    }

    /**
     * 根据ID查询例句
     * GET /api/examples/{id}
     */
    @GetMapping("/{id}")
    public ExampleVO findById(@PathVariable Integer id) {
        return exampleService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("例句不存在: " + id));
    }

    /**
     * 检查例句是否已存在
     * GET /api/examples/exists?jp=私は学生です。&excludeId=1
     */
    @AdviceIgnore
    @GetMapping("/exists")
    public Map<String, Object> checkExists(
            @RequestParam String jp,
            @RequestParam(required = false) Integer excludeId) {
        Integer existingId = exampleService.checkExists(jp, excludeId);
        if (existingId != null) {
            return Map.of("exists", true, "existingId", existingId);
        }
        return Map.of("exists", false);
    }

    /**
     * AI分析日语例句，自动创建不存在的单词和语法
     * POST /api/examples/ai-analyze
     */
    @PostMapping("/ai-analyze")
    public AiAnalyzeResult aiAnalyze(@Valid @RequestBody AiAnalyzeRequest request) {
        return exampleAiService.analyze(request.getJp());
    }

    /**
     * 创建例句
     * POST /api/examples
     */
    @PostMapping
    public ExampleVO create(@Valid @RequestBody ExampleRequest request) {
        return exampleService.create(request);
    }

    /**
     * 更新例句
     * PUT /api/examples/{id}
     */
    @PutMapping("/{id}")
    public ExampleVO update(@PathVariable Integer id, @Valid @RequestBody ExampleRequest request) {
        return exampleService.update(id, request);
    }

    /**
     * 删除例句
     * DELETE /api/examples/{id}
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        exampleService.delete(id);
    }

    /**
     * 重新生成单个例句音频
     * POST /api/examples/{id}/regenerate-audio
     */
    @PostMapping("/{id}/regenerate-audio")
    public Map<String, Object> regenerateAudio(@PathVariable Integer id) {
        boolean success = exampleService.regenerateAudio(id);
        return Map.of("success", success);
    }

    /**
     * 创建批量例句音频生成任务（异步）
     * POST /api/examples/regenerate-all-audio
     * Body (optional): { "ids": [1,2,3], "onlyMissing": true }
     * Returns a task that can be monitored via GET /api/tasks/{taskId}
     */
    @PostMapping("/regenerate-all-audio")
    public TaskVO createAudioTask(@RequestBody(required = false) Map<String, Object> params) {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTaskType("EXAMPLE_AUDIO");
        request.setParams(params != null ? params : Map.of());
        return taskService.create(request);
    }

    /**
     * 启动已创建的音频任务
     * POST /api/examples/regenerate-all-audio/{taskId}/start
     */
    @PostMapping("/regenerate-all-audio/{taskId}/start")
    public TaskVO startAudioTask(@PathVariable Integer taskId) {
        return taskService.start(taskId);
    }
}