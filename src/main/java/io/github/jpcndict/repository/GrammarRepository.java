package io.github.jpcndict.repository;

import io.github.jpcndict.entity.GrammarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrammarRepository extends JpaRepository<GrammarEntity, Integer> {

    /**
     * 根据语法条目精确查询
     */
    Optional<GrammarEntity> findByWord(String word);

    /**
     * 根据读音精确查询
     */
    Optional<GrammarEntity> findByReading(String reading);

    /**
     * 根据语法条目模糊查询
     */
    List<GrammarEntity> findByWordContaining(String word);

    /**
     * 根据读音模糊查询
     */
    List<GrammarEntity> findByReadingContaining(String reading);

    /**
     * 根据语法条目或读音模糊查询
     */
    List<GrammarEntity> findByWordContainingOrReadingContaining(String word, String reading);
}
