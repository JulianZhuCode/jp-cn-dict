package io.github.jpcndict.repository;

import io.github.jpcndict.entity.ExamplesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamplesRepository extends JpaRepository<ExamplesEntity, Integer>, JpaSpecificationExecutor<ExamplesEntity> {

    List<ExamplesEntity> findByJpContaining(String jp);

    List<ExamplesEntity> findByCnContaining(String cn);

    List<ExamplesEntity> findByJpContainingOrCnContaining(String jp, String cn);

    boolean existsByJp(String jp);

    Optional<ExamplesEntity> findByJp(String jp);

    boolean existsByJpAndIdNot(String jp, Integer id);
}