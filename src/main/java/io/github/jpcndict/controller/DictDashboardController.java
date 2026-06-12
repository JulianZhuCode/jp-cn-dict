package io.github.jpcndict.controller;

import io.github.jpcndict.service.DictImportService;
import io.github.springwhale.framework.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST API for dict dashboard operations (import).
 */
@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
@Slf4j
public class DictDashboardController {

    private final DictImportService dictImportService;

    @PostMapping("/import/words")
    public Integer importWords(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return 0;
            }
            return dictImportService.importWords(file.getInputStream());
        } catch (Exception e) {
            log.error("words 文件导入失败", e);
            throw BusinessException.create("IMPORT_ERROR", "导入失败，请检查文件内容与格式");
        }
    }

    @PostMapping("/import/grammars")
    public Integer importGrammars(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return 0;
            }
            return dictImportService.importGrammars(file.getInputStream());
        } catch (Exception e) {
            log.error("grammars 文件导入失败", e);
            throw BusinessException.create("IMPORT_ERROR", "导入失败，请检查文件内容与格式");
        }
    }
}
