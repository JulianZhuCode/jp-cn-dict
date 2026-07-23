package io.github.jpcndict.controller.page;

import io.github.jpcndict.dto.vo.ExampleVO;
import io.github.jpcndict.dto.vo.GrammarVO;
import io.github.jpcndict.dto.vo.WordVO;
import io.github.jpcndict.enums.WordPos;
import io.github.jpcndict.repository.ExampleRepository;
import io.github.jpcndict.repository.GrammarRepository;
import io.github.jpcndict.repository.WordRepository;
import io.github.jpcndict.service.ExampleService;
import io.github.jpcndict.service.GrammarService;
import io.github.jpcndict.service.WordService;
import io.github.springwhale.database.SortUtils;
import io.github.springwhale.framework.thymeleaf.controller.AdminPage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Admin console page controller for Dict module.
 * <p>
 * Serves Thymeleaf templates under {@code templates/admin/dict/}.
 * REST API endpoints remain under {@code /api/words, /api/grammars, /api/examples}.
 * </p>
 */
@AdminPage
@Controller
@RequestMapping("/admin/dict")
@RequiredArgsConstructor
public class DictPageController {

    private final WordService wordService;
    private final GrammarService grammarService;
    private final ExampleService exampleService;
    private final WordRepository wordRepository;
    private final GrammarRepository grammarRepository;
    private final ExampleRepository exampleRepository;

    // ---- Dashboard ----

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        model.addAttribute("wordCount", wordRepository.count());
        model.addAttribute("grammarCount", grammarRepository.count());
        model.addAttribute("exampleCount", exampleRepository.count());
        return "admin/dict/dashboard";
    }

    // ---- Words ----

    @GetMapping("/words")
    public String words(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String pos,
                        @RequestParam(required = false) String sort,
                        Model model) {
        Sort sortObj = SortUtils.buildSort(sort);
        Page<WordVO> wordPage = wordService.search(keyword, pos, PageRequest.of(page, size, sortObj));
        model.addAttribute("words", wordPage.getContent());
        model.addAttribute("page", wordPage);
        model.addAttribute("allPos", WordPos.values());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedPos", pos);
        model.addAttribute("sortField", SortUtils.getSortField(sortObj));
        model.addAttribute("sortDirection", SortUtils.getSortDirection(sortObj));
        return "admin/dict/words";
    }

    // ---- Grammars ----

    @GetMapping("/grammars")
    public String grammars(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String keyword,
                           @RequestParam(required = false) String sort,
                           Model model) {
        Sort sortObj = SortUtils.buildSort(sort);
        Page<GrammarVO> grammarPage = grammarService.search(keyword, PageRequest.of(page, size, sortObj));
        model.addAttribute("grammars", grammarPage.getContent());
        model.addAttribute("page", grammarPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortField", SortUtils.getSortField(sortObj));
        model.addAttribute("sortDirection", SortUtils.getSortDirection(sortObj));
        return "admin/dict/grammars";
    }

    // ---- Examples ----

    @GetMapping("/examples")
    public String examples(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String keyword,
                           @RequestParam(required = false) String sort,
                           Model model) {
        Sort sortObj = SortUtils.buildSort(sort);
        Page<ExampleVO> examplePage = exampleService.search(keyword, PageRequest.of(page, size, sortObj));
        model.addAttribute("examples", examplePage.getContent());
        model.addAttribute("page", examplePage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortField", SortUtils.getSortField(sortObj));
        model.addAttribute("sortDirection", SortUtils.getSortDirection(sortObj));
        return "admin/dict/examples";
    }

}
