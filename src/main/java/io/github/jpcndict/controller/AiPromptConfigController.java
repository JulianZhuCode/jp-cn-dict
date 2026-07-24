package io.github.jpcndict.controller;

import io.github.jpcndict.dto.request.AiPromptConfigRequest;
import io.github.jpcndict.dto.vo.AiPromptConfigVO;
import io.github.jpcndict.service.AiPromptConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-prompt-configs")
@RequiredArgsConstructor
public class AiPromptConfigController {

    private final AiPromptConfigService service;

    /**
     * 分页查询所有AI提示词配置
     * GET /api/ai-prompt-configs?page=0&size=20&keyword=
     */
    @GetMapping
    public Page<AiPromptConfigVO> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        if (keyword != null && !keyword.trim().isEmpty()) {
            return service.search(keyword, pageable);
        }
        return service.findAll(pageable);
    }

    /**
     * 根据ID查询配置
     * GET /api/ai-prompt-configs/{id}
     */
    @GetMapping("/{id}")
    public AiPromptConfigVO findById(@PathVariable Integer id) {
        return service.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("提示词配置不存在，ID: " + id));
    }

    /**
     * 根据提示词key查询配置
     * GET /api/ai-prompt-configs/key/{promptKey}
     */
    @GetMapping("/key/{promptKey}")
    public AiPromptConfigVO findByKey(@PathVariable String promptKey) {
        return service.findByKey(promptKey)
                .orElseThrow(() -> new IllegalArgumentException("提示词配置不存在，key: " + promptKey));
    }

    /**
     * 创建配置
     * POST /api/ai-prompt-configs
     */
    @PostMapping
    public AiPromptConfigVO create(@Valid @RequestBody AiPromptConfigRequest request) {
        return service.create(request);
    }

    /**
     * 更新配置
     * PUT /api/ai-prompt-configs/{id}
     */
    @PutMapping("/{id}")
    public AiPromptConfigVO update(@PathVariable Integer id, @Valid @RequestBody AiPromptConfigRequest request) {
        return service.update(id, request);
    }

    /**
     * 删除配置
     * DELETE /api/ai-prompt-configs/{id}
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

    /**
     * 刷新缓存
     * POST /api/ai-prompt-configs/refresh-cache
     */
    @PostMapping("/refresh-cache")
    public void refreshCache() {
        service.refreshCache();
    }
}
