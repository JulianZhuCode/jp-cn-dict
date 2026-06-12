package io.github.jpcndict.controller.page;

import io.github.jpcndict.dto.vo.ExamplesVO;
import io.github.jpcndict.dto.vo.GrammarVO;
import io.github.jpcndict.dto.vo.WordVO;
import io.github.jpcndict.repository.ExamplesRepository;
import io.github.jpcndict.repository.GrammarRepository;
import io.github.jpcndict.repository.WordRepository;
import io.github.jpcndict.service.ExamplesService;
import io.github.jpcndict.service.GrammarService;
import io.github.jpcndict.service.WordService;
import io.github.springwhale.framework.thymeleaf.controller.AdminPage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final ExamplesService examplesService;
    private final WordRepository wordRepository;
    private final GrammarRepository grammarRepository;
    private final ExamplesRepository examplesRepository;

    // ---- Dashboard ----

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        model.addAttribute("wordCount", wordRepository.count());
        model.addAttribute("grammarCount", grammarRepository.count());
        model.addAttribute("exampleCount", examplesRepository.count());
        return "admin/dict/dashboard";
    }

    // ---- Words ----

    @GetMapping("/words")
    public String words(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "15") int size,
                        Model model) {
        Page<WordVO> wordPage = wordService.findAll(PageRequest.of(page, size));
        model.addAttribute("words", wordPage.getContent());
        model.addAttribute("page", wordPage);
        return "admin/dict/words";
    }

    @GetMapping("/words/create")
    public String wordCreate() {
        return "admin/dict/word-form";
    }

    @GetMapping("/words/{id:\\d+}")
    public String wordDetail(@PathVariable Integer id, Model model) {
        model.addAttribute("word", wordService.findById(id).orElse(null));
        return "admin/dict/word-detail";
    }

    // ---- Grammars ----

    @GetMapping("/grammars")
    public String grammars(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "15") int size,
                           Model model) {
        Page<GrammarVO> grammarPage = grammarService.findAll(PageRequest.of(page, size));
        model.addAttribute("grammars", grammarPage.getContent());
        model.addAttribute("page", grammarPage);
        return "admin/dict/grammars";
    }

    @GetMapping("/grammars/create")
    public String grammarCreate() {
        return "admin/dict/grammar-form";
    }

    @GetMapping("/grammars/{id:\\d+}")
    public String grammarDetail(@PathVariable Integer id, Model model) {
        model.addAttribute("grammar", grammarService.findById(id).orElse(null));
        return "admin/dict/grammar-detail";
    }

    // ---- Examples ----

    @GetMapping("/examples")
    public String examples(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "15") int size,
                           Model model) {
        Page<ExamplesVO> examplePage = examplesService.findAll(PageRequest.of(page, size));
        model.addAttribute("examples", examplePage.getContent());
        model.addAttribute("page", examplePage);
        return "admin/dict/examples";
    }

    @GetMapping("/examples/create")
    public String exampleCreate() {
        return "admin/dict/example-form";
    }

    @GetMapping("/examples/{id:\\d+}")
    public String exampleDetail(@PathVariable Integer id, Model model) {
        model.addAttribute("example", examplesService.findById(id).orElse(null));
        return "admin/dict/example-detail";
    }
}
