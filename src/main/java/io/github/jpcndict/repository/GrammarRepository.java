package io.github.jpcndict.repository;

import io.github.jpcndict.entity.GrammarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrammarRepository extends JpaRepository<GrammarEntity, Integer>, JpaSpecificationExecutor<GrammarEntity> {

    Optional<GrammarEntity> findByPattern(String pattern);

    Optional<GrammarEntity> findByReading(String reading);

    List<GrammarEntity> findByPatternContaining(String pattern);

    List<GrammarEntity> findByReadingContaining(String reading);

    List<GrammarEntity> findByPatternContainingOrReadingContaining(String pattern, String reading);

    /**
     * 批量查询语法（用于AI分析场景）
     */
    List<GrammarEntity> findByPatternIn(List<String> patterns);
}