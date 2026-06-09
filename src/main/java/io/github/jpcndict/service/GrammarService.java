package io.github.jpcndict.service;

import io.github.springwhale.framework.core.exception.BusinessException;
import io.github.jpcndict.dto.request.GrammarRequest;
import io.github.jpcndict.dto.vo.GrammarVO;
import io.github.jpcndict.entity.GrammarEntity;
import io.github.jpcndict.mapper.GrammarMapper;
import io.github.jpcndict.repository.GrammarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Optional<GrammarVO> findByWord(String word) {
        return grammarRepository.findByWord(word).map(grammarMapper::toVO);
    }

    /**
     * 搜索语法（支持条目或读音模糊查询）
     */
    public List<GrammarVO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return grammarMapper.toVOList(grammarRepository.findByWordContainingOrReadingContaining(keyword, keyword));
    }

    /**
     * 创建语法
     */
    @Transactional
    public GrammarVO create(GrammarRequest request) {
        GrammarEntity entity = new GrammarEntity();
        entity.setWord(request.getWord());
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

        grammar.setWord(request.getWord());
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
}
