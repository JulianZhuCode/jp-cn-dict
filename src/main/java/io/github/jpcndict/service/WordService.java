package io.github.jpcndict.service;

import io.github.jpcndict.dto.request.WordRequest;
import io.github.jpcndict.dto.vo.WordVO;
import io.github.jpcndict.entity.WordEntity;
import io.github.jpcndict.mapper.WordMapper;
import io.github.jpcndict.repository.WordRepository;
import io.github.springwhale.framework.core.exception.BusinessException;
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
}
