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
    private final AudioService audioService;

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
     * 根据罗马音精确查询（内存中过滤，因为 romaji 不持久化在 DB 中）
     */
    public Optional<WordVO> findByRomaji(String romaji) {
        if (romaji == null || romaji.isEmpty()) {
            return Optional.empty();
        }
        return wordRepository.findAll().stream()
                .filter(w -> romaji.equalsIgnoreCase(w.getRomaji()))
                .findFirst()
                .map(wordMapper::toVO);
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
     * <p>
     * 因为 romaji 字段不再存储在数据库中（@Transient 动态生成），
     * 所以：数据库仅按 word/reading 模糊查询 + 词性筛选，
     * 再在结果中对 romaji 做忽略大小写的 contains 匹配（合并去重）。
     */
    public Page<WordVO> search(String keyword, String pos, Pageable pageable) {
        // 1) DB 查询：按 word/reading + pos 筛选
        var spec = JpaQueryWrapper.of(WordEntity.class)
                .or(!ObjectUtils.isEmpty(keyword), w -> w
                        .likeIgnoreCase(WordEntity::getWord, keyword)
                        .likeIgnoreCase(WordEntity::getReading, keyword))
                .eq(!ObjectUtils.isEmpty(pos), WordEntity::getPos, pos)
                .buildSpec();

        // 若关键字为空则直接走 DB 分页
        if (ObjectUtils.isEmpty(keyword)) {
            return wordRepository.findAll(spec, pageable).map(wordMapper::toVO);
        }

        // 2) 有 keyword 时，需要从 DB 查 word/reading 匹配，并补 romaji 内存匹配
        List<WordEntity> dbList = wordRepository.findAll(spec);
        String kwLower = keyword.toLowerCase();

        // 3) 从全量词库中补 romaji 匹配的词（按同样的 pos 过滤），避免分页结果遗漏
        //    为了避免重复：先收集 dbList 中已有的 id
        java.util.Set<Integer> seenIds = dbList.stream()
                .map(WordEntity::getId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        // 判断单个实体是否满足 romaji 匹配
        java.util.function.Predicate<WordEntity> romajiMatch = w -> {
            String r = w.getRomaji();
            if (r == null) return false;
            return r.toLowerCase().contains(kwLower);
        };

        List<WordEntity> romajiExtras = new ArrayList<>();
        if (!ObjectUtils.isEmpty(pos)) {
            // pos 相同时直接在 pos 集合中找
            List<WordEntity> posList = wordRepository.findByPos(pos);
            for (WordEntity w : posList) {
                if (!seenIds.contains(w.getId()) && romajiMatch.test(w)) {
                    romajiExtras.add(w);
                    seenIds.add(w.getId());
                }
            }
        } else {
            // pos 空 -> 在全量中匹配 romaji（避免 findAll 太频繁，只在已查出 dbList 的 seenIds 基础上扫一次全量即可）
            for (WordEntity w : wordRepository.findAll()) {
                if (!seenIds.contains(w.getId()) && romajiMatch.test(w)) {
                    romajiExtras.add(w);
                    seenIds.add(w.getId());
                }
            }
        }

        // 4) 合并结果并手动分页（按 pageable 的排序如果是 Spring 默认 id，这里简化按 id 排序；与 DB 默认策略一致）
        List<WordEntity> merged = new ArrayList<>(dbList.size() + romajiExtras.size());
        merged.addAll(dbList);
        merged.addAll(romajiExtras);
        merged.sort(java.util.Comparator.comparing(WordEntity::getId, java.util.Comparator.nullsLast(Integer::compareTo)));

        int total = merged.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);
        List<WordEntity> pageContent = (start >= total) ? List.of() : merged.subList(start, end);

        List<WordVO> voContent = pageContent.stream().map(wordMapper::toVO).toList();
        return new org.springframework.data.domain.PageImpl<>(voContent, pageable, total);
    }

    /**
     * 创建单词
     */
    @Transactional
    public WordVO create(WordRequest request) {
        WordEntity entity = new WordEntity();
        entity.setWord(request.getWord());
        entity.setReading(request.getReading());
        // romaji 由 reading 动态生成，无需手动设置
        entity.setMeaning(request.getMeaning());
        entity.setNotes(request.getNotes());
        entity.setPos(request.getPos());
        WordEntity saved = wordRepository.save(entity);

        String audioUrl = audioService.generateWordAudio(saved.getId(), saved.getWord());
        if (audioUrl != null) {
            saved.setAudioUrl(audioUrl);
            wordRepository.save(saved);
        }

        return wordMapper.toVO(saved);
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
        // romaji 由 reading 动态生成，无需手动设置
        word.setMeaning(request.getMeaning());
        word.setNotes(request.getNotes());
        word.setPos(request.getPos());

        String audioUrl = audioService.generateWordAudio(word.getId(), word.getWord());
        if (audioUrl != null) {
            word.setAudioUrl(audioUrl);
        }

        return wordMapper.toVO(wordRepository.save(word));
    }

    /**
     * 删除单词
     */
    @Transactional
    public void delete(Integer id) {
        WordEntity word = wordRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("WORD_NOT_FOUND", "单词不存在，ID: " + id));
        audioService.deleteWordAudio(id);
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
        // romaji 由 reading 动态生成，无需手动设置
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
                    // romaji 由 reading 动态生成，无需手动设置
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

    /**
     * 批量保存单词（用于批量更新音频URL等）
     */
    @Transactional
    public List<WordEntity> saveAll(List<WordEntity> entities) {
        if (entities == null || entities.isEmpty()) return List.of();
        return wordRepository.saveAll(entities);
    }

    /**
     * 重新生成指定单词的音频
     */
    @Transactional
    public boolean regenerateAudio(Integer id) {
        WordEntity word = wordRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("WORD_NOT_FOUND", "单词不存在，ID: " + id));
        String audioUrl = audioService.generateWordAudio(word.getId(), word.getWord());
        if (audioUrl != null) {
            word.setAudioUrl(audioUrl);
            wordRepository.save(word);
            return true;
        }
        return false;
    }

    /**
     * 批量重新生成所有单词的音频（并发）
     */
    @Transactional
    public int regenerateAllAudio() {
        List<WordEntity> all = wordRepository.findAll();
        if (all.isEmpty()) {
            return 0;
        }

        List<Integer> wordIds = all.stream().map(WordEntity::getId).toList();
        List<String> wordTexts = all.stream().map(WordEntity::getWord).toList();

        List<String> urls = audioService.batchGenerateWordAudio(wordIds, wordTexts);

        int success = 0;
        for (int i = 0; i < all.size(); i++) {
            String url = urls.get(i);
            if (url != null) {
                all.get(i).setAudioUrl(url);
                success++;
            }
        }
        if (success > 0) {
            wordRepository.saveAll(all);
        }
        return success;
    }
}
