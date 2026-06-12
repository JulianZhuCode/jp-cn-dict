package io.github.jpcndict.controller;

import io.github.jpcndict.service.DictImportService;
import io.github.springwhale.framework.core.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * REST API for dict dashboard operations (import / export).
 */
@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
@Slf4j
public class DictDashboardController {

    private final DictImportService dictImportService;

    @PostMapping("/import/words")
    public Integer importWords(@RequestParam("files") MultipartFile[] files) {
        try {
            return dictImportService.importWords(extractStreams(files));
        } catch (Exception e) {
            log.error("words 文件导入失败", e);
            throw BusinessException.create("IMPORT_ERROR", "导入失败，请检查文件内容与格式");
        }
    }

    @PostMapping("/import/grammars")
    public Integer importGrammars(@RequestParam("files") MultipartFile[] files) {
        try {
            return dictImportService.importGrammars(extractStreams(files));
        } catch (Exception e) {
            log.error("grammars 文件导入失败", e);
            throw BusinessException.create("IMPORT_ERROR", "导入失败，请检查文件内容与格式");
        }
    }

    private static List<InputStream> extractStreams(MultipartFile[] files) throws IOException {
        List<InputStream> streams = new ArrayList<>();
        for (MultipartFile f : files) {
            if (f != null && !f.isEmpty()) {
                streams.add(f.getInputStream());
            }
        }
        return streams;
    }

    @GetMapping("/export/words")
    public void exportWords(HttpServletResponse response) {
        try {
            byte[] zipData = dictImportService.exportWords();
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=words.zip");
            response.getOutputStream().write(zipData);
        } catch (IOException e) {
            log.error("words 导出失败", e);
            throw BusinessException.create("EXPORT_ERROR", "导出失败");
        }
    }

    @GetMapping("/export/grammars")
    public void exportGrammars(HttpServletResponse response) {
        try {
            byte[] zipData = dictImportService.exportGrammars();
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=grammars.zip");
            response.getOutputStream().write(zipData);
        } catch (IOException e) {
            log.error("grammars 导出失败", e);
            throw BusinessException.create("EXPORT_ERROR", "导出失败");
        }
    }
}
