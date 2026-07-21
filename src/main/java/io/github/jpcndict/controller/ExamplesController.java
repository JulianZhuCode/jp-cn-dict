package io.github.jpcndict.controller;

import io.github.jpcndict.dto.request.ExamplesRequest;
import io.github.jpcndict.dto.vo.ExamplesVO;
import io.github.jpcndict.service.ExamplesService;
import io.github.springwhale.framework.webmvc.advice.AdviceIgnore;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/examples")
@RequiredArgsConstructor
public class ExamplesController {

    private final ExamplesService examplesService;

    /**
     * 分页查询所有例句（支持筛选）
     * GET /api/examples?page=0&size=20&keyword=
     */
    @GetMapping
    public Page<ExamplesVO> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        return examplesService.search(keyword, pageable);
    }

    /**
     * 根据ID查询例句
     * GET /api/examples/{id}
     */
    @GetMapping("/{id}")
    public ExamplesVO findById(@PathVariable Integer id) {
        return examplesService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("例句不存在: " + id));
    }

    /**
     * 检查例句是否已存在
     * GET /api/examples/exists?jp=私は学生です。&excludeId=1
     */
    @AdviceIgnore
    @GetMapping("/exists")
    public java.util.Map<String, Object> checkExists(
            @RequestParam String jp,
            @RequestParam(required = false) Integer excludeId) {
        Integer existingId = examplesService.checkExists(jp, excludeId);
        if (existingId != null) {
            return java.util.Map.of("exists", true, "existingId", existingId);
        }
        return java.util.Map.of("exists", false);
    }

    /**
     * 创建例句
     * POST /api/examples
     */
    @PostMapping
    public ExamplesVO create(@Valid @RequestBody ExamplesRequest request) {
        return examplesService.create(request);
    }

    /**
     * 更新例句
     * PUT /api/examples/{id}
     */
    @PutMapping("/{id}")
    public ExamplesVO update(@PathVariable Integer id, @Valid @RequestBody ExamplesRequest request) {
        return examplesService.update(id, request);
    }

    /**
     * 删除例句
     * DELETE /api/examples/{id}
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        examplesService.delete(id);
    }
}
