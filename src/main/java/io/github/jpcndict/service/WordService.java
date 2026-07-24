package io.github.jpcndict.service;

import io.github.jpcndict.dto.request.WordRequest;
import io.github.jpcndict.dto.vo.AiAnalyzeResult;
import io.github.jpcndict.dto.vo.WordVO;
import io.github.jpcndict.entity.WordEntity;
import io.github.jpcndict.mapper.WordMapper;
import io.github.jpcndict.repository.WordRepository;
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
public class WordService {

    private final WordRepository wordRepository;
    private final WordMapper wordMapper;

    /**
     * 分页查询所有单词
     */
    public Page<WordVO> findAll(Pageable pageable) {
        return wordRepository.findAll(pageable).map(wordMapper::toVO);
    }

    /**
     * 根据ID查询单词
     */
    public Optional<WordVO> findById(Integer id) {
        return wordRepository.findById(id).map(wordMapper::toVO);
    }

    /**
     * 根据单词精确查询
     */
    public Optional<WordVO> findByWord(String word) {
        return wordRepository.findByWord(word).map(wordMapper::toVO);
    }

    /**
     * 根据读音精确查询
     */
    public Optional<WordVO> findByReading(String reading) {
        return wordRepository.findByReading(reading).map(wordMapper::toVO);
    }

    /**
     * 根据罗马音精确查询
     */
    public Optional<WordVO> findByRomaji(String romaji) {
        return wordRepository.findByRomaji(romaji).map(wordMapper::toVO);
    }

    /**
     * 搜索单词（支持单词或读音模糊查询）
     */
    public List<WordVO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return wordMapper.toVOList(wordRepository.findByWordContainingOrReadingContaining(keyword, keyword));
    }

    /**
     * 根据词性查询
     */
    public List<WordVO> findByPos(String pos) {
        return wordMapper.toVOList(wordRepository.findByPos(pos));
    }

    /**
     * 分页搜索单词（支持关键字模糊搜索 + 词性筛选）
     */
    public Page<WordVO> search(String keyword, String pos, Pageable pageable) {
        var spec = JpaQueryWrapper.of(WordEntity.class)
                .or(!ObjectUtils.isEmpty(keyword), w -> w
                        .likeIgnoreCase(WordEntity::getWord, keyword)
                        .likeIgnoreCase(WordEntity::getReading, keyword)
                        .likeIgnoreCase(WordEntity::getRomaji, keyword))
                .eq(!ObjectUtils.isEmpty(pos), WordEntity::getPos, pos)
                .buildSpec();
        return wordRepository.findAll(spec, pageable).map(wordMapper::toVO);
    }

    /**
     * 创建单词
     */
    @Transactional
    public WordVO create(WordRequest request) {
        WordEntity entity = new WordEntity();
        entity.setWord(request.getWord());
        entity.setReading(request.getReading());
        entity.setRomaji(request.getRomaji());
        entity.setMeaning(request.getMeaning());
        entity.setNotes(request.getNotes());
        entity.setPos(request.getPos());
        return wordMapper.toVO(wordRepository.save(entity));
    }

    /**
     * 更新单词
     */
    @Transactional
    public WordVO update(Integer id, WordRequest request) {
        WordEntity word = wordRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("WORD_NOT_FOUND", "单词不存在，ID: " + id));

        word.setWord(request.getWord());
        word.setReading(request.getReading());
        word.setRomaji(request.getRomaji());
        word.setMeaning(request.getMeaning());
        word.setNotes(request.getNotes());
        word.setPos(request.getPos());

        return wordMapper.toVO(wordRepository.save(word));
    }

    /**
     * 删除单词
     */
    @Transactional
    public void delete(Integer id) {
        WordEntity word = wordRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("WORD_NOT_FOUND", "单词不存在，ID: " + id));
        wordRepository.delete(word);
    }

    /**
     * 查找或创建单词（用于AI分析场景）
     * 如果单词已存在则返回已有的，否则创建新单词
     */
    @Transactional
    public AiAnalyzeResult.WordAnalysis findOrCreate(AiAnalyzeResult.WordAnalysis wordAnalysis) {
        Optional<WordEntity> existing = wordRepository.findByWord(wordAnalysis.getWord());
        if (existing.isPresent()) {
            WordEntity e = existing.get();
            return AiAnalyzeResult.WordAnalysis.builder()
                    .id(e.getId())
                    .word(e.getWord())
                    .reading(e.getReading())
                    .pos(e.getPos())
                    .meaning(e.getMeaning() != null ? List.of(e.getMeaning()) : List.of())
                    .build();
        }

        // 创建新单词
        WordEntity entity = new WordEntity();
        entity.setWord(wordAnalysis.getWord());
        entity.setReading(wordAnalysis.getReading());
        entity.setPos(wordAnalysis.getPos());
        entity.setMeaning(wordAnalysis.getMeaning() != null ? wordAnalysis.getMeaning().toArray(new String[0]) : null);
        entity.setIsManualConfirmed(false);
        WordEntity saved = wordRepository.save(entity);

        return AiAnalyzeResult.WordAnalysis.builder()
                .id(saved.getId())
                .word(saved.getWord())
                .reading(saved.getReading())
                .pos(saved.getPos())
                .meaning(saved.getMeaning() != null ? List.of(saved.getMeaning()) : List.of())
                .build();
    }
}
