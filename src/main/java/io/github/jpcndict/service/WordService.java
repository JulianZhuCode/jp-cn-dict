package io.github.jpcndict.service;

import io.github.jpcndict.dto.request.WordRequest;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
     * 查找或创建单词（通用方法）
     * 如果单词已存在则返回已有的，否则创建新单词
     */
    @Transactional
    public WordEntity findOrCreate(WordEntity entity) {
        if (entity.getWord() == null || entity.getWord().trim().isEmpty()) {
            throw new IllegalArgumentException("单词不能为空");
        }

        Optional<WordEntity> existing = wordRepository.findByWord(entity.getWord());
        if (existing.isPresent()) {
            return existing.get();
        }

        // 创建新单词，设置默认值
        WordEntity newEntity = new WordEntity();
        newEntity.setWord(entity.getWord());
        newEntity.setReading(entity.getReading());
        newEntity.setRomaji(entity.getRomaji());
        newEntity.setPos(entity.getPos());
        newEntity.setMeaning(entity.getMeaning());
        newEntity.setNotes(entity.getNotes());

        return wordRepository.save(newEntity);
    }

    /**
     * 批量查找或创建单词（通用方法）
     * 优化：1次批量查询 + 1次批量插入
     */
    @Transactional
    public List<WordEntity> findOrCreateBatch(List<WordEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        // 1. 提取所有单词名称
        List<String> wordNames = entities.stream()
                .map(WordEntity::getWord)
                .filter(word -> word != null && !word.trim().isEmpty())
                .toList();

        if (wordNames.isEmpty()) {
            return List.of();
        }

        // 2. 一次批量查询所有已存在的单词
        List<WordEntity> existingWords = wordRepository.findByWordIn(wordNames);

        // 3. 构建已存在单词的映射（word -> WordEntity）
        Map<String, WordEntity> existingMap = existingWords.stream()
                .collect(java.util.stream.Collectors.toMap(
                        WordEntity::getWord,
                        e -> e,
                        (e1, e2) -> e1 // 如果有重复，保留第一个
                ));

        // 4. 分离出需要创建的新单词
        List<WordEntity> newEntities = new ArrayList<>();
        for (var entity : entities) {
            if (entity.getWord() != null && !entity.getWord().trim().isEmpty()) {
                if (!existingMap.containsKey(entity.getWord())) {
                    WordEntity newEntity = new WordEntity();
                    newEntity.setWord(entity.getWord());
                    newEntity.setReading(entity.getReading());
                    newEntity.setRomaji(entity.getRomaji());
                    newEntity.setPos(entity.getPos());
                    newEntity.setMeaning(entity.getMeaning());
                    newEntity.setNotes(entity.getNotes());
                    newEntities.add(newEntity);
                }
            }
        }

        // 5. 批量保存新单词
        if (!newEntities.isEmpty()) {
            List<WordEntity> savedEntities = wordRepository.saveAll(newEntities);
            // 将新保存的单词加入映射
            for (var saved : savedEntities) {
                existingMap.put(saved.getWord(), saved);
            }
        }

        // 6. 构建结果列表（保持原顺序）
        return entities.stream()
                .filter(entity -> entity.getWord() != null && !entity.getWord().trim().isEmpty())
                .map(entity -> existingMap.get(entity.getWord()))
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}
