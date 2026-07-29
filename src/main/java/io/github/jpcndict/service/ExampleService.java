package io.github.jpcndict.service;

import io.github.jpcndict.dto.request.ExampleRequest;
import io.github.jpcndict.dto.vo.ExampleVO;
import io.github.jpcndict.dto.vo.GrammarVO;
import io.github.jpcndict.dto.vo.WordVO;
import io.github.jpcndict.entity.ExampleEntity;
import io.github.jpcndict.mapper.ExampleMapper;
import io.github.jpcndict.repository.ExampleRepository;
import io.github.jpcndict.repository.GrammarRepository;
import io.github.jpcndict.repository.WordRepository;
import io.github.springwhale.database.JpaQueryWrapper;
import io.github.springwhale.framework.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExampleService {

    private final ExampleRepository exampleRepository;
    private final ExampleMapper exampleMapper;
    private final WordRepository wordRepository;
    private final GrammarRepository grammarRepository;
    private final AudioService audioService;

    /**
     * 分页查询所有例句
     */
    public Page<ExampleVO> findAll(Pageable pageable) {
        return exampleRepository.findAll(pageable).map(entity -> {
            ExampleVO vo = exampleMapper.toVO(entity);
            enrichRelatedDetails(vo);
            return vo;
        });
    }

    /**
     * 根据ID查询例句
     */
    public Optional<ExampleVO> findById(Integer id) {
        return exampleRepository.findById(id).map(entity -> {
            ExampleVO vo = exampleMapper.toVO(entity);
            enrichRelatedDetails(vo);
            return vo;
        });
    }

    /**
     * 搜索例句（支持日语或中文模糊查询）
     */
    public List<ExampleVO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        List<ExampleVO> vos = exampleMapper.toVOList(exampleRepository.findByJpContainingOrCnContaining(keyword, keyword));
        vos.forEach(this::enrichRelatedDetails);
        return vos;
    }

    /**
     * 分页搜索例句（支持关键字模糊搜索）
     */
    public Page<ExampleVO> search(String keyword, Pageable pageable) {
        var spec = JpaQueryWrapper.of(ExampleEntity.class)
                .or(!ObjectUtils.isEmpty(keyword), w -> w
                        .likeIgnoreCase(ExampleEntity::getJp, keyword)
                        .likeIgnoreCase(ExampleEntity::getCn, keyword))
                .buildSpec();
        return exampleRepository.findAll(spec, pageable).map(entity -> {
            ExampleVO vo = exampleMapper.toVO(entity);
            enrichRelatedDetails(vo);
            return vo;
        });
    }

    /**
     * 检查例句是否已存在
     *
     * @param jp        日语例句
     * @param excludeId 排除的ID（编辑时排除自身）
     * @return 已存在时返回该记录ID，否则返回null
     */
    public Integer checkExists(String jp, Integer excludeId) {
        if (jp == null || jp.trim().isEmpty()) {
            return null;
        }
        if (excludeId != null) {
            if (exampleRepository.existsByJpAndIdNot(jp.trim(), excludeId)) {
                return exampleRepository.findByJp(jp.trim()).map(ExampleEntity::getId).orElse(null);
            }
            return null;
        }
        return exampleRepository.findByJp(jp.trim()).map(ExampleEntity::getId).orElse(null);
    }

    /**
     * 创建例句
     */
    @Transactional
    public ExampleVO create(ExampleRequest request) {
        if (checkExists(request.getJp(), null) != null) {
            throw BusinessException.create("EXAMPLE_EXISTS", "该日语例句已存在");
        }
        ExampleEntity entity = new ExampleEntity();
        entity.setJp(request.getJp());
        entity.setCn(request.getCn());
        entity.setRelatedWords(request.getRelatedWords());
        entity.setRelatedGrammars(request.getRelatedGrammars());
        ExampleEntity saved = exampleRepository.save(entity);

        String audioUrl = audioService.generateExampleAudio(saved.getId(), saved.getJp());
        if (audioUrl != null) {
            saved.setAudioUrl(audioUrl);
            exampleRepository.save(saved);
        }

        ExampleVO vo = exampleMapper.toVO(saved);
        enrichRelatedDetails(vo);
        return vo;
    }

    /**
     * 更新例句
     */
    @Transactional
    public ExampleVO update(Integer id, ExampleRequest request) {
        ExampleEntity example = exampleRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("EXAMPLE_NOT_FOUND", "例句不存在，ID: " + id));

        if (checkExists(request.getJp(), id) != null) {
            throw BusinessException.create("EXAMPLE_EXISTS", "该日语例句已存在");
        }

        example.setJp(request.getJp());
        example.setCn(request.getCn());
        example.setRelatedWords(request.getRelatedWords());
        example.setRelatedGrammars(request.getRelatedGrammars());

        String audioUrl = audioService.generateExampleAudio(example.getId(), example.getJp());
        if (audioUrl != null) {
            example.setAudioUrl(audioUrl);
        }

        ExampleVO vo = exampleMapper.toVO(exampleRepository.save(example));
        enrichRelatedDetails(vo);
        return vo;
    }

    /**
     * 删除例句
     */
    @Transactional
    public void delete(Integer id) {
        ExampleEntity example = exampleRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("EXAMPLE_NOT_FOUND", "例句不存在，ID: " + id));
        audioService.deleteExampleAudio(id);
        exampleRepository.delete(example);
    }

    /**
     * 填充关联单词和语法的详情信息（用于前端展示）
     */
    private void enrichRelatedDetails(ExampleVO vo) {
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
                        gv.setPattern(g.getPattern());
                        gv.setReading(g.getReading());
                        return gv;
                    })
                    .toList());
        }
    }

    /**
     * 重新生成指定例句的音频
     */
    @Transactional
    public boolean regenerateAudio(Integer id) {
        ExampleEntity example = exampleRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("EXAMPLE_NOT_FOUND", "例句不存在，ID: " + id));
        String audioUrl = audioService.generateExampleAudio(example.getId(), example.getJp());
        if (audioUrl != null) {
            example.setAudioUrl(audioUrl);
            exampleRepository.save(example);
            return true;
        }
        return false;
    }

    /**
     * 批量重新生成所有例句的音频
     */
    @Transactional
    public int regenerateAllAudio() {
        List<ExampleEntity> all = exampleRepository.findAll();
        int success = 0;
        for (ExampleEntity example : all) {
            String audioUrl = audioService.generateExampleAudio(example.getId(), example.getJp());
            if (audioUrl != null) {
                example.setAudioUrl(audioUrl);
                success++;
            }
        }
        if (success > 0) {
            exampleRepository.saveAll(all);
        }
        return success;
    }
}