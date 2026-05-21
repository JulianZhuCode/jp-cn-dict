package io.github.jpcndict.controller;

import io.github.jpcndict.dto.ExamplesDTO;
import io.github.jpcndict.service.ExamplesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Page<ExamplesDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(examplesService.findAll(pageable));
    }

    /**
     * 根据ID查询例句
     * GET /api/examples/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExamplesDTO> findById(@PathVariable Integer id) {
        return examplesService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 搜索例句（模糊查询）
     * GET /api/examples/search?keyword=取引
     */
    @GetMapping("/search")
    public ResponseEntity<List<ExamplesDTO>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(examplesService.search(keyword));
    }

    /**
     * 创建例句
     * POST /api/examples
     */
    @PostMapping
    public ResponseEntity<ExamplesDTO> create(@RequestBody ExamplesDTO exampleDTO) {
        return ResponseEntity.ok(examplesService.create(exampleDTO));
    }

    /**
     * 更新例句
     * PUT /api/examples/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ExamplesDTO> update(@PathVariable Integer id, @RequestBody ExamplesDTO exampleDTO) {
        return ResponseEntity.ok(examplesService.update(id, exampleDTO));
    }

    /**
     * 删除例句
     * DELETE /api/examples/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        examplesService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
