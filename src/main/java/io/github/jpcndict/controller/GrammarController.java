package io.github.jpcndict.controller;

import io.github.jpcndict.dto.request.GrammarRequest;
import io.github.jpcndict.dto.vo.GrammarVO;
import io.github.jpcndict.service.GrammarService;
import io.github.springwhale.framework.webmvc.advice.AdviceIgnore;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grammars")
@RequiredArgsConstructor
public class GrammarController {

    private final GrammarService grammarService;

    /**
     * 分页查询所有语法（支持筛选）
     * GET /api/grammars?page=0&size=20&keyword=
     */
    @GetMapping
    public Page<GrammarVO> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        return grammarService.search(keyword, pageable);
    }

    /**
     * 根据ID查询语法
     * GET /api/grammars/{id}
     */
    @GetMapping("/{id}")
    public GrammarVO findById(@PathVariable Integer id) {
        return grammarService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("语法不存在: " + id));
    }

    /**
     * 根据语法条目精确查询
     * GET /api/grammars/search/by-word?word=～ている
     */
    @GetMapping("/search/by-word")
    public GrammarVO findByWord(@RequestParam String word) {
        return grammarService.findByWord(word)
                .orElseThrow(() -> new IllegalArgumentException("语法不存在: " + word));
    }

    /**
     * 搜索语法（模糊查询）
     * GET /api/grammars/search?keyword=～て
     */
    @AdviceIgnore
    @GetMapping("/search")
    public List<GrammarVO> search(@RequestParam String keyword) {
        return grammarService.search(keyword);
    }

    /**
     * 创建语法
     * POST /api/grammars
     */
    @PostMapping
    public GrammarVO create(@Valid @RequestBody GrammarRequest request) {
        return grammarService.create(request);
    }

    /**
     * 更新语法
     * PUT /api/grammars/{id}
     */
    @PutMapping("/{id}")
    public GrammarVO update(@PathVariable Integer id, @Valid @RequestBody GrammarRequest request) {
        return grammarService.update(id, request);
    }

    /**
     * 删除语法
     * DELETE /api/grammars/{id}
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        grammarService.delete(id);
    }
}
