package io.github.jpcndict.controller;

import io.github.jpcndict.dto.WordDTO;
import io.github.jpcndict.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    /**
     * 分页查询所有单词
     * GET /api/words?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<WordDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(wordService.findAll(pageable));
    }

    /**
     * 根据ID查询单词
     * GET /api/words/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<WordDTO> findById(@PathVariable Integer id) {
        return wordService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据单词精确查询
     * GET /api/words/search/by-word?word=日本語
     */
    @GetMapping("/search/by-word")
    public ResponseEntity<WordDTO> findByWord(@RequestParam String word) {
        return wordService.findByWord(word)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据读音精确查询
     * GET /api/words/search/by-reading?reading=にほんご
     */
    @GetMapping("/search/by-reading")
    public ResponseEntity<WordDTO> findByReading(@RequestParam String reading) {
        return wordService.findByReading(reading)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据罗马音精确查询
     * GET /api/words/search/by-romaji?romaji=nihongo
     */
    @GetMapping("/search/by-romaji")
    public ResponseEntity<WordDTO> findByRomaji(@RequestParam String romaji) {
        return wordService.findByRomaji(romaji)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 搜索单词（模糊查询）
     * GET /api/words/search?keyword=日本
     */
    @GetMapping("/search")
    public ResponseEntity<List<WordDTO>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(wordService.search(keyword));
    }

    /**
     * 根据词性查询
     * GET /api/words/by-pos?pos=NOUN
     */
    @GetMapping("/by-pos")
    public ResponseEntity<List<WordDTO>> findByPos(@RequestParam String pos) {
        return ResponseEntity.ok(wordService.findByPos(pos));
    }

    /**
     * 创建单词
     * POST /api/words
     */
    @PostMapping
    public ResponseEntity<WordDTO> create(@RequestBody WordDTO wordDTO) {
        return ResponseEntity.ok(wordService.create(wordDTO));
    }

    /**
     * 更新单词
     * PUT /api/words/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<WordDTO> update(@PathVariable Integer id, @RequestBody WordDTO wordDTO) {
        return ResponseEntity.ok(wordService.update(id, wordDTO));
    }

    /**
     * 删除单词
     * DELETE /api/words/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        wordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
