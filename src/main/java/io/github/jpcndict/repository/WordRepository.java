package io.github.jpcndict.repository;

import io.github.jpcndict.entity.WordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
