package io.github.jpcndict.service;

import io.github.jpcndict.dto.request.GrammarRequest;
import io.github.jpcndict.dto.vo.GrammarVO;
import io.github.jpcndict.entity.GrammarEntity;
import io.github.jpcndict.mapper.GrammarMapper;
import io.github.jpcndict.repository.GrammarRepository;
import io.github.springwhale.database.JpaQueryWrapper;
import io.github.springwhale.framework.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GrammarService {

    private final GrammarRepository grammarRepository;
    private final GrammarMapper grammarMapper;

    /**
     * 分页查询所有语法
     */
    public Page<GrammarVO> findAll(Pageable pageable) {
        return grammarRepository.findAll(pageable).map(grammarMapper::toVO);
    }

    /**
     * 根据ID查询语法
     */
    public Optional<GrammarVO> findById(Integer id) {
        return grammarRepository.findById(id).map(grammarMapper::toVO);
    }

    /**
     * 根据语法条目精确查询
     */
    public Optional<GrammarVO> findByPattern(String pattern) {
        return grammarRepository.findByPattern(pattern).map(grammarMapper::toVO);
    }

    /**
     * 搜索语法（支持条目或读音模糊查询）
     */
    public List<GrammarVO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return grammarMapper.toVOList(grammarRepository.findByPatternContainingOrReadingContaining(keyword, keyword));
    }

    /**
     * 分页搜索语法（支持关键字模糊搜索）
     */
    public Page<GrammarVO> search(String keyword, Pageable pageable) {
        var spec = JpaQueryWrapper.of(GrammarEntity.class)
                .or(!ObjectUtils.isEmpty(keyword), w -> w
                        .likeIgnoreCase(GrammarEntity::getPattern, keyword)
                        .likeIgnoreCase(GrammarEntity::getReading, keyword))
                .buildSpec();
        return grammarRepository.findAll(spec, pageable).map(grammarMapper::toVO);
    }

    /**
     * 创建语法
     */
    @Transactional
    public GrammarVO create(GrammarRequest request) {
        GrammarEntity entity = new GrammarEntity();
        entity.setPattern(request.getPattern());
        entity.setReading(request.getReading());
        entity.setMeaning(request.getMeaning());
        entity.setNotes(request.getNotes());
        return grammarMapper.toVO(grammarRepository.save(entity));
    }

    /**
     * 更新语法
     */
    @Transactional
    public GrammarVO update(Integer id, GrammarRequest request) {
        GrammarEntity grammar = grammarRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("GRAMMAR_NOT_FOUND", "语法不存在，ID: " + id));

        grammar.setPattern(request.getPattern());
        grammar.setReading(request.getReading());
        grammar.setMeaning(request.getMeaning());
        grammar.setNotes(request.getNotes());

        return grammarMapper.toVO(grammarRepository.save(grammar));
    }

    /**
     * 删除语法
     */
    @Transactional
    public void delete(Integer id) {
        GrammarEntity grammar = grammarRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("GRAMMAR_NOT_FOUND", "语法不存在，ID: " + id));
        grammarRepository.delete(grammar);
    }

    /**
     * 查找或创建语法（通用方法）
     * 如果语法已存在则返回已有的，否则创建新语法
     */
    @Transactional
    public GrammarEntity findOrCreate(GrammarEntity entity) {
        if (entity.getPattern() == null || entity.getPattern().trim().isEmpty()) {
            throw new IllegalArgumentException("语法模式不能为空");
        }

        Optional<GrammarEntity> existing = grammarRepository.findByPattern(entity.getPattern());
        if (existing.isPresent()) {
            return existing.get();
        }

        // 创建新语法，设置默认值
        GrammarEntity newEntity = new GrammarEntity();
        newEntity.setPattern(entity.getPattern());
        newEntity.setReading(entity.getReading());
        newEntity.setMeaning(entity.getMeaning());
        newEntity.setNotes(entity.getNotes());

        return grammarRepository.save(newEntity);
    }

    /**
     * 批量查找或创建语法（通用方法）
     * 优化：1次批量查询 + 1次批量插入
     */
    @Transactional
    public List<GrammarEntity> findOrCreateBatch(List<GrammarEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        // 1. 提取所有语法模式
        List<String> patterns = entities.stream()
                .map(GrammarEntity::getPattern)
                .filter(pattern -> pattern != null && !pattern.trim().isEmpty())
                .toList();

        if (patterns.isEmpty()) {
            return List.of();
        }

        // 2. 一次批量查询所有已存在的语法
        List<GrammarEntity> existingGrammars = grammarRepository.findByPatternIn(patterns);

        // 3. 构建已存在语法的映射（pattern -> GrammarEntity）
        Map<String, GrammarEntity> existingMap = existingGrammars.stream()
                .collect(java.util.stream.Collectors.toMap(
                        GrammarEntity::getPattern,
                        e -> e,
                        (e1, e2) -> e1 // 如果有重复，保留第一个
                ));

        // 4. 分离出需要创建的新语法
        List<GrammarEntity> newEntities = new ArrayList<>();
        for (var entity : entities) {
            if (entity.getPattern() != null && !entity.getPattern().trim().isEmpty()) {
                if (!existingMap.containsKey(entity.getPattern())) {
                    GrammarEntity newEntity = new GrammarEntity();
                    newEntity.setPattern(entity.getPattern());
                    newEntity.setReading(entity.getReading());
                    newEntity.setMeaning(entity.getMeaning());
                    newEntity.setNotes(entity.getNotes());
                    newEntities.add(newEntity);
                }
            }
        }

        // 5. 批量保存新语法
        if (!newEntities.isEmpty()) {
            List<GrammarEntity> savedEntities = grammarRepository.saveAll(newEntities);
            // 将新保存的语法加入映射
            for (var saved : savedEntities) {
                existingMap.put(saved.getPattern(), saved);
            }
        }

        // 6. 构建结果列表（保持原顺序）
        return entities.stream()
                .filter(entity -> entity.getPattern() != null && !entity.getPattern().trim().isEmpty())
                .map(entity -> existingMap.get(entity.getPattern()))
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}
