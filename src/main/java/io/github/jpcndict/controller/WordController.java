package io.github.jpcndict.controller;

import io.github.jpcndict.dto.request.WordRequest;
import io.github.jpcndict.dto.vo.WordVO;
import io.github.jpcndict.service.WordService;
import io.github.springwhale.framework.webmvc.advice.AdviceIgnore;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

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
}
