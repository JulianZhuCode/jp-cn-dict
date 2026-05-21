package io.github.jpcndict.controller;

import io.github.jpcndict.dto.GrammarDTO;
import io.github.jpcndict.service.GrammarService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grammars")
@RequiredArgsConstructor
public class GrammarController {

    private final GrammarService grammarService;

    /**
     * 分页查询所有语法
     * GET /api/grammars?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<GrammarDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(grammarService.findAll(pageable));
    }

    /**
     * 根据ID查询语法
     * GET /api/grammars/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<GrammarDTO> findById(@PathVariable Integer id) {
        return grammarService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据语法条目精确查询
     * GET /api/grammars/search/by-word?word=～ている
     */
    @GetMapping("/search/by-word")
    public ResponseEntity<GrammarDTO> findByWord(@RequestParam String word) {
        return grammarService.findByWord(word)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 搜索语法（模糊查询）
     * GET /api/grammars/search?keyword=～て
     */
    @GetMapping("/search")
    public ResponseEntity<List<GrammarDTO>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(grammarService.search(keyword));
    }

    /**
     * 创建语法
     * POST /api/grammars
     */
    @PostMapping
    public ResponseEntity<GrammarDTO> create(@RequestBody GrammarDTO grammarDTO) {
        return ResponseEntity.ok(grammarService.create(grammarDTO));
    }

    /**
     * 更新语法
     * PUT /api/grammars/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<GrammarDTO> update(@PathVariable Integer id, @RequestBody GrammarDTO grammarDTO) {
        return ResponseEntity.ok(grammarService.update(id, grammarDTO));
    }

    /**
     * 删除语法
     * DELETE /api/grammars/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        grammarService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
