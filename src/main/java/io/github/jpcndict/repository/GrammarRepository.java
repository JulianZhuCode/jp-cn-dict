package io.github.jpcndict.repository;

import io.github.jpcndict.entity.GrammarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrammarRepository extends JpaRepository<GrammarEntity, Integer>, JpaSpecificationExecutor<GrammarEntity> {

    Optional<GrammarEntity> findByWord(String word);

    Optional<GrammarEntity> findByReading(String reading);

    List<GrammarEntity> findByWordContaining(String word);

    List<GrammarEntity> findByReadingContaining(String reading);

    List<GrammarEntity> findByWordContainingOrReadingContaining(String word, String reading);
}