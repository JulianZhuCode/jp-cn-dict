package io.github.jpcndict.service;

import io.github.jpcndict.dto.request.GrammarRequest;
import io.github.jpcndict.dto.vo.AiAnalyzeResult;
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

import java.util.List;
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
     * 查找或创建语法（用于AI分析场景）
     * 如果语法已存在则返回已有的，否则创建新语法
     */
    @Transactional
    public AiAnalyzeResult.GrammarAnalysis findOrCreate(AiAnalyzeResult.GrammarAnalysis grammarAnalysis) {
        Optional<GrammarEntity> existing = grammarRepository.findByPattern(grammarAnalysis.getPattern());
        if (existing.isPresent()) {
            GrammarEntity e = existing.get();
            return AiAnalyzeResult.GrammarAnalysis.builder()
                    .id(e.getId())
                    .pattern(e.getPattern())
                    .reading(e.getReading())
                    .meaning(e.getMeaning() != null ? List.of(e.getMeaning()) : List.of())
                    .build();
        }

        // 创建新语法
        GrammarEntity entity = new GrammarEntity();
        entity.setPattern(grammarAnalysis.getPattern());
        entity.setReading(grammarAnalysis.getReading());
        entity.setMeaning(grammarAnalysis.getMeaning() != null ? grammarAnalysis.getMeaning().toArray(new String[0]) : null);
        entity.setIsManualConfirmed(false);
        GrammarEntity saved = grammarRepository.save(entity);

        return AiAnalyzeResult.GrammarAnalysis.builder()
                .id(saved.getId())
                .pattern(saved.getPattern())
                .reading(saved.getReading())
                .meaning(saved.getMeaning() != null ? List.of(saved.getMeaning()) : List.of())
                .build();
    }
}
