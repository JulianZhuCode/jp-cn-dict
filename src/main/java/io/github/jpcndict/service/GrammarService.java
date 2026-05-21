package io.github.jpcndict.service;

import io.github.jpcndict.dto.GrammarDTO;
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
    public Page<GrammarDTO> findAll(Pageable pageable) {
        return grammarRepository.findAll(pageable).map(grammarMapper::toDTO);
    }

    /**
     * 根据ID查询语法
     */
    public Optional<GrammarDTO> findById(Integer id) {
        return grammarRepository.findById(id).map(grammarMapper::toDTO);
    }

    /**
     * 根据语法条目精确查询
     */
    public Optional<GrammarDTO> findByWord(String word) {
        return grammarRepository.findByWord(word).map(grammarMapper::toDTO);
    }

    /**
     * 搜索语法（支持条目或读音模糊查询）
     */
    public List<GrammarDTO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return grammarMapper.toDTOList(grammarRepository.findByWordContainingOrReadingContaining(keyword, keyword));
    }

    /**
     * 创建语法
     */
    @Transactional
    public GrammarDTO create(GrammarDTO grammarDTO) {
        GrammarEntity entity = grammarMapper.toEntity(grammarDTO);
        return grammarMapper.toDTO(grammarRepository.save(entity));
    }

    /**
     * 更新语法
     */
    @Transactional
    public GrammarDTO update(Integer id, GrammarDTO grammarDTO) {
        GrammarEntity grammar = grammarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("语法不存在，ID: " + id));

        grammar.setWord(grammarDTO.getWord());
        grammar.setReading(grammarDTO.getReading());
        grammar.setMeaning(grammarDTO.getMeaning());
        grammar.setNotes(grammarDTO.getNotes());

        return grammarMapper.toDTO(grammarRepository.save(grammar));
    }

    /**
     * 删除语法
     */
    @Transactional
    public void delete(Integer id) {
        GrammarEntity grammar = grammarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("语法不存在，ID: " + id));
        grammarRepository.delete(grammar);
    }
}
