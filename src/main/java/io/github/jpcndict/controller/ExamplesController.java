package io.github.jpcndict.controller;

import io.github.jpcndict.dto.request.ExamplesRequest;
import io.github.jpcndict.dto.vo.ExamplesVO;
import io.github.jpcndict.service.ExamplesService;
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
     * 分页查询所有例句
     * GET /api/examples?page=0&size=20
     */
    @GetMapping
    public Page<ExamplesVO> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return examplesService.findAll(pageable);
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
     * 搜索例句（模糊查询）
     * GET /api/examples/search?keyword=取引
     */
    @GetMapping("/search")
    public List<ExamplesVO> search(@RequestParam String keyword) {
        return examplesService.search(keyword);
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
