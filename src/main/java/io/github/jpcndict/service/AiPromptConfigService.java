package io.github.jpcndict.service;

import io.github.jpcndict.dao.entity.AiPromptConfigEntity;
import io.github.jpcndict.dao.repository.AiPromptConfigRepository;
import io.github.jpcndict.dto.request.AiPromptConfigRequest;
import io.github.jpcndict.dto.vo.AiPromptConfigVO;
import io.github.jpcndict.mapper.AiPromptConfigMapper;
import io.github.springwhale.database.JpaQueryWrapper;
import io.github.springwhale.framework.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AiPromptConfigService {

    private static final String CACHE_NAME = "aiPromptConfig";

    private final AiPromptConfigRepository repository;
    private final AiPromptConfigMapper mapper;

    /**
     * 分页查询所有配置
     */
    public Page<AiPromptConfigVO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toVO);
    }

    /**
     * 根据ID查询配置
     */
    public Optional<AiPromptConfigVO> findById(Integer id) {
        return repository.findById(id).map(mapper::toVO);
    }

    /**
     * 根据提示词key查询（带缓存）
     */
    @Cacheable(value = CACHE_NAME, key = "#promptKey")
    public Optional<AiPromptConfigVO> findByKey(String promptKey) {
        return repository.findByPromptKey(promptKey).map(mapper::toVO);
    }


    /**
     * 搜索配置（支持关键字模糊搜索）
     */
    public Page<AiPromptConfigVO> search(String keyword, Pageable pageable) {
        var spec = JpaQueryWrapper.of(AiPromptConfigEntity.class)
                .or(!ObjectUtils.isEmpty(keyword), w -> w
                        .likeIgnoreCase(AiPromptConfigEntity::getPromptKey, keyword)
                        .likeIgnoreCase(AiPromptConfigEntity::getPromptName, keyword))
                .buildSpec();
        return repository.findAll(spec, pageable).map(mapper::toVO);
    }

    /**
     * 创建配置
     */
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public AiPromptConfigVO create(AiPromptConfigRequest request) {
        // 检查key是否重复
        if (repository.existsByPromptKey(request.getPromptKey())) {
            throw BusinessException.create("PROMPT_KEY_EXISTS", "提示词标识已存在: " + request.getPromptKey());
        }

        AiPromptConfigEntity entity = new AiPromptConfigEntity();
        entity.setPromptKey(request.getPromptKey());
        entity.setPromptName(request.getPromptName());
        entity.setSystemPrompt(request.getSystemPrompt());
        entity.setUserPromptTemplate(request.getUserPromptTemplate());
        entity.setModelName(request.getModelName());
        entity.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);

        return mapper.toVO(repository.save(entity));
    }

    /**
     * 更新配置
     */
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public AiPromptConfigVO update(Integer id, AiPromptConfigRequest request) {
        AiPromptConfigEntity entity = repository.findById(id)
                .orElseThrow(() -> BusinessException.create("PROMPT_CONFIG_NOT_FOUND", "提示词配置不存在，ID: " + id));

        // 检查key是否与其他配置重复
        if (repository.existsByPromptKeyAndIdNot(request.getPromptKey(), id)) {
            throw BusinessException.create("PROMPT_KEY_EXISTS", "提示词标识已存在: " + request.getPromptKey());
        }

        mapper.updateFromRequest(request, entity);
        if (request.getEnabled() == null) {
            entity.setEnabled(true);
        }

        return mapper.toVO(repository.save(entity));
    }

    /**
     * 删除配置
     */
    @Transactional
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void delete(Integer id) {
        AiPromptConfigEntity entity = repository.findById(id)
                .orElseThrow(() -> BusinessException.create("PROMPT_CONFIG_NOT_FOUND", "提示词配置不存在，ID: " + id));
        repository.delete(entity);
    }

    /**
     * 刷新缓存
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void refreshCache() {
        log.info("AI提示词配置缓存已刷新");
    }
}
