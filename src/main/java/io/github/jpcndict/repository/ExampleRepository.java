package io.github.jpcndict.repository;

import io.github.jpcndict.entity.ExampleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExampleRepository extends JpaRepository<ExampleEntity, Integer>, JpaSpecificationExecutor<ExampleEntity> {

    List<ExampleEntity> findByJpContaining(String jp);

    List<ExampleEntity> findByCnContaining(String cn);

    List<ExampleEntity> findByJpContainingOrCnContaining(String jp, String cn);

    boolean existsByJp(String jp);

    Optional<ExampleEntity> findByJp(String jp);

    boolean existsByJpAndIdNot(String jp, Integer id);
}