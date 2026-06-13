package io.github.jpcndict.service;

import io.github.jpcndict.dto.request.ExamplesRequest;
import io.github.jpcndict.dto.vo.ExamplesVO;
import io.github.jpcndict.dto.vo.GrammarVO;
import io.github.jpcndict.dto.vo.WordVO;
import io.github.jpcndict.entity.ExamplesEntity;
import io.github.jpcndict.mapper.ExamplesMapper;
import io.github.jpcndict.repository.ExamplesRepository;
import io.github.jpcndict.repository.GrammarRepository;
import io.github.jpcndict.repository.WordRepository;
import io.github.springwhale.framework.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamplesService {

    private final ExamplesRepository examplesRepository;
    private final ExamplesMapper examplesMapper;
    private final WordRepository wordRepository;
    private final GrammarRepository grammarRepository;

    /**
     * 分页查询所有例句
     */
    public Page<ExamplesVO> findAll(Pageable pageable) {
        return examplesRepository.findAll(pageable).map(entity -> {
            ExamplesVO vo = examplesMapper.toVO(entity);
            enrichRelatedDetails(vo);
            return vo;
        });
    }

    /**
     * 根据ID查询例句
     */
    public Optional<ExamplesVO> findById(Integer id) {
        return examplesRepository.findById(id).map(entity -> {
            ExamplesVO vo = examplesMapper.toVO(entity);
            enrichRelatedDetails(vo);
            return vo;
        });
    }

    /**
     * 搜索例句（支持日语或中文模糊查询）
     */
    public List<ExamplesVO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        List<ExamplesVO> vos = examplesMapper.toVOList(examplesRepository.findByJpContainingOrCnContaining(keyword, keyword));
        vos.forEach(this::enrichRelatedDetails);
        return vos;
    }

    /**
     * 创建例句
     */
    @Transactional
    public ExamplesVO create(ExamplesRequest request) {
        ExamplesEntity entity = new ExamplesEntity();
        entity.setJp(request.getJp());
        entity.setCn(request.getCn());
        entity.setRelatedWords(request.getRelatedWords());
        entity.setRelatedGrammars(request.getRelatedGrammars());
        ExamplesEntity saved = examplesRepository.save(entity);
        ExamplesVO vo = examplesMapper.toVO(saved);
        enrichRelatedDetails(vo);
        return vo;
    }

    /**
     * 更新例句
     */
    @Transactional
    public ExamplesVO update(Integer id, ExamplesRequest request) {
        ExamplesEntity example = examplesRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("EXAMPLE_NOT_FOUND", "例句不存在，ID: " + id));

        example.setJp(request.getJp());
        example.setCn(request.getCn());
        example.setRelatedWords(request.getRelatedWords());
        example.setRelatedGrammars(request.getRelatedGrammars());

        ExamplesVO vo = examplesMapper.toVO(examplesRepository.save(example));
        enrichRelatedDetails(vo);
        return vo;
    }

    /**
     * 删除例句
     */
    @Transactional
    public void delete(Integer id) {
        ExamplesEntity example = examplesRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("EXAMPLE_NOT_FOUND", "例句不存在，ID: " + id));
        examplesRepository.delete(example);
    }

    /**
     * 填充关联单词和语法的详情信息（用于前端展示）
     */
    private void enrichRelatedDetails(ExamplesVO vo) {
        if (vo.getRelatedWords() != null && vo.getRelatedWords().length > 0) {
            List<Integer> wordIds = Arrays.asList(vo.getRelatedWords());
            vo.setRelatedWordItems(wordRepository.findAllById(wordIds).stream()
                    .map(w -> {
                        WordVO wv = new WordVO();
                        wv.setId(w.getId());
                        wv.setWord(w.getWord());
                        wv.setReading(w.getReading());
                        return wv;
                    })
                    .toList());
        }
        if (vo.getRelatedGrammars() != null && vo.getRelatedGrammars().length > 0) {
            List<Integer> grammarIds = Arrays.asList(vo.getRelatedGrammars());
            vo.setRelatedGrammarItems(grammarRepository.findAllById(grammarIds).stream()
                    .map(g -> {
                        GrammarVO gv = new GrammarVO();
                        gv.setId(g.getId());
                        gv.setWord(g.getWord());
                        gv.setReading(g.getReading());
                        return gv;
                    })
                    .toList());
        }
    }
}
