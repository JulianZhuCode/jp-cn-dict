package io.github.jpcndict.service;

import io.github.jpcndict.dto.WordDTO;
import io.github.jpcndict.entity.WordEntity;
import io.github.jpcndict.mapper.WordMapper;
import io.github.jpcndict.repository.WordRepository;
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
    public Page<WordDTO> findAll(Pageable pageable) {
        return wordRepository.findAll(pageable).map(wordMapper::toDTO);
    }

    /**
     * 根据ID查询单词
     */
    public Optional<WordDTO> findById(Integer id) {
        return wordRepository.findById(id).map(wordMapper::toDTO);
    }

    /**
     * 根据单词精确查询
     */
    public Optional<WordDTO> findByWord(String word) {
        return wordRepository.findByWord(word).map(wordMapper::toDTO);
    }

    /**
     * 根据读音精确查询
     */
    public Optional<WordDTO> findByReading(String reading) {
        return wordRepository.findByReading(reading).map(wordMapper::toDTO);
    }

    /**
     * 根据罗马音精确查询
     */
    public Optional<WordDTO> findByRomaji(String romaji) {
        return wordRepository.findByRomaji(romaji).map(wordMapper::toDTO);
    }

    /**
     * 搜索单词（支持单词或读音模糊查询）
     */
    public List<WordDTO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return wordMapper.toDTOList(wordRepository.findByWordContainingOrReadingContaining(keyword, keyword));
    }

    /**
     * 根据词性查询
     */
    public List<WordDTO> findByPos(String pos) {
        return wordMapper.toDTOList(wordRepository.findByPos(pos));
    }

    /**
     * 创建单词
     */
    @Transactional
    public WordDTO create(WordDTO wordDTO) {
        WordEntity entity = wordMapper.toEntity(wordDTO);
        return wordMapper.toDTO(wordRepository.save(entity));
    }

    /**
     * 更新单词
     */
    @Transactional
    public WordDTO update(Integer id, WordDTO wordDTO) {
        WordEntity word = wordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("单词不存在，ID: " + id));

        word.setWord(wordDTO.getWord());
        word.setReading(wordDTO.getReading());
        word.setRomaji(wordDTO.getRomaji());
        word.setMeaning(wordDTO.getMeaning());
        word.setNotes(wordDTO.getNotes());
        word.setPos(wordDTO.getPos());

        return wordMapper.toDTO(wordRepository.save(word));
    }

    /**
     * 删除单词
     */
    @Transactional
    public void delete(Integer id) {
        WordEntity word = wordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("单词不存在，ID: " + id));
        wordRepository.delete(word);
    }
}
