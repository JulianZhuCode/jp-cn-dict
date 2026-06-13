package io.github.jpcndict.repository;

import io.github.jpcndict.entity.ExamplesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamplesRepository extends JpaRepository<ExamplesEntity, Integer> {

    /**
     * 根据日语例句模糊查询
     */
    List<ExamplesEntity> findByJpContaining(String jp);

    /**
     * 根据中文翻译模糊查询
     */
    List<ExamplesEntity> findByCnContaining(String cn);

    /**
     * 根据日语或中文模糊查询
     */
    List<ExamplesEntity> findByJpContainingOrCnContaining(String jp, String cn);

    /**
     * 精确匹配日语例句（用于存在性检查）
     */
    boolean existsByJp(String jp);

    /**
     * 精确匹配日语例句（用于获取已存在记录ID）
     */
    Optional<ExamplesEntity> findByJp(String jp);

    /**
     * 精确匹配日语例句，排除指定ID（编辑时使用）
     */
    boolean existsByJpAndIdNot(String jp, Integer id);
}
