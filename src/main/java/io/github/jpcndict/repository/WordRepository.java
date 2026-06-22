package io.github.jpcndict.repository;

import io.github.jpcndict.entity.WordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WordRepository extends JpaRepository<WordEntity, Integer> {

    /**
     * 根据单词精确查询
     */
    Optional<WordEntity> findByWord(String word);

    /**
     * 根据读音精确查询
     */
    Optional<WordEntity> findByReading(String reading);

    /**
     * 根据罗马音精确查询
     */
    Optional<WordEntity> findByRomaji(String romaji);

    /**
     * 根据单词模糊查询
     */
    List<WordEntity> findByWordContaining(String word);

    /**
     * 根据读音模糊查询
     */
    List<WordEntity> findByReadingContaining(String reading);

    /**
     * 根据词性查询
     */
    List<WordEntity> findByPos(String pos);

    /**
     * 根据单词或读音模糊查询
     */
    List<WordEntity> findByWordContainingOrReadingContaining(String word, String reading);

    /**
     * 分页模糊搜索：根据单词、读音、罗马音、含义模糊匹配
     */
    @Query("SELECT w FROM WordEntity w WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(w.word) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(w.reading) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(w.romaji) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<WordEntity> search(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 分页模糊搜索 + 词性筛选
     */
    @Query("SELECT w FROM WordEntity w WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(w.word) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(w.reading) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(w.romaji) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:pos IS NULL OR :pos = '' OR w.pos = :pos)")
    Page<WordEntity> searchWithPos(@Param("keyword") String keyword, @Param("pos") String pos, Pageable pageable);
}
