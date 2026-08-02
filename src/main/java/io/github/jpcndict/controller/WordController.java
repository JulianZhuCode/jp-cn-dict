package io.github.jpcndict.controller;

import io.github.jpcndict.dto.request.WordRequest;
import io.github.jpcndict.dto.vo.WordVO;
import io.github.jpcndict.service.WordService;
import io.github.springwhale.framework.webmvc.advice.AdviceIgnore;
import io.github.springwhale.platform.task.dto.request.TaskCreateRequest;
import io.github.springwhale.platform.task.dto.vo.TaskVO;
import io.github.springwhale.platform.task.enums.TaskStatus;
import io.github.springwhale.platform.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;
    private final TaskService taskService;

    /**
     * 分页查询所有单词（支持筛选）
     * GET /api/words?page=0&size=20&keyword=&pos=
     */
    @GetMapping
    public Page<WordVO> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String pos) {
        Pageable pageable = PageRequest.of(page, size);
        return wordService.search(keyword, pos, pageable);
    }

    /**
     * 根据ID查询单词
     * GET /api/words/{id}
     */
    @GetMapping("/{id}")
    public WordVO findById(@PathVariable Integer id) {
        return wordService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("单词不存在: " + id));
    }

    /**
     * 根据单词精确查询
     * GET /api/words/search/by-word?word=日本語
     */
    @GetMapping("/search/by-word")
    public WordVO findByWord(@RequestParam String word) {
        return wordService.findByWord(word)
                .orElseThrow(() -> new IllegalArgumentException("单词不存在: " + word));
    }

    /**
     * 根据读音精确查询
     * GET /api/words/search/by-reading?reading=にほんご
     */
    @GetMapping("/search/by-reading")
    public WordVO findByReading(@RequestParam String reading) {
        return wordService.findByReading(reading)
                .orElseThrow(() -> new IllegalArgumentException("单词不存在: " + reading));
    }

    /**
     * 根据罗马音精确查询
     * GET /api/words/search/by-romaji?romaji=nihongo
     */
    @GetMapping("/search/by-romaji")
    public WordVO findByRomaji(@RequestParam String romaji) {
        return wordService.findByRomaji(romaji)
                .orElseThrow(() -> new IllegalArgumentException("单词不存在: " + romaji));
    }

    /**
     * 搜索单词（模糊查询）
     * GET /api/words/search?keyword=日本
     */
    @AdviceIgnore
    @GetMapping("/search")
    public List<WordVO> search(@RequestParam String keyword) {
        return wordService.search(keyword);
    }

    /**
     * 根据词性查询
     * GET /api/words/by-pos?pos=NOUN
     */
    @GetMapping("/by-pos")
    public List<WordVO> findByPos(@RequestParam String pos) {
        return wordService.findByPos(pos);
    }

    /**
     * 创建单词
     * POST /api/words
     */
    @PostMapping
    public WordVO create(@Valid @RequestBody WordRequest request) {
        return wordService.create(request);
    }

    /**
     * 更新单词
     * PUT /api/words/{id}
     */
    @PutMapping("/{id}")
    public WordVO update(@PathVariable Integer id, @Valid @RequestBody WordRequest request) {
        return wordService.update(id, request);
    }

    /**
     * 删除单词
     * DELETE /api/words/{id}
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        wordService.delete(id);
    }

    /**
     * 重新生成单个单词音频
     * POST /api/words/{id}/regenerate-audio
     */
    @PostMapping("/{id}/regenerate-audio")
    public Map<String, Object> regenerateAudio(@PathVariable Integer id) {
        boolean success = wordService.regenerateAudio(id);
        return Map.of("success", success);
    }

    /**
     * 创建并启动批量单词音频生成任务（异步）
     * POST /api/words/regenerate-all-audio
     * Body (optional): { "ids": [1,2,3], "onlyMissing": true }
     * Returns task info with ID for monitoring at /tasks page
     */
    @PostMapping("/regenerate-all-audio")
    public TaskVO createAndStartAudioTask(@RequestBody(required = false) Map<String, Object> params) {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTaskType("WORD_AUDIO");
        request.setParams(params != null ? params : Map.of());
        TaskVO task = taskService.create(request);
        if (task.getStatus() == TaskStatus.PENDING) {
            task = taskService.start(task.getId());
        }
        return task;
    }

    /**
     * 启动已创建的音频任务
     * POST /api/words/regenerate-all-audio/{taskId}/start
     */
    @PostMapping("/regenerate-all-audio/{taskId}/start")
    public TaskVO startAudioTask(@PathVariable Integer taskId) {
        return taskService.start(taskId);
    }
}