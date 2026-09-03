package io.github.jpcndict.dao.repository;

import io.github.jpcndict.dao.entity.WordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WordRepository extends JpaRepository<WordEntity, Integer>, JpaSpecificationExecutor<WordEntity> {

    Optional<WordEntity> findByWord(String word);

    Optional<WordEntity> findByReading(String reading);

    List<WordEntity> findByWordContaining(String word);

    List<WordEntity> findByReadingContaining(String reading);

    List<WordEntity> findByPos(String pos);

    List<WordEntity> findByWordContainingOrReadingContaining(String word, String reading);

    /**
     * 批量查询单词（用于AI分析场景）
     */
    List<WordEntity> findByWordIn(List<String> words);
}