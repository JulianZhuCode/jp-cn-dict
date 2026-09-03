package io.github.jpcndict.dao.repository;

import io.github.jpcndict.dao.entity.AiPromptConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiPromptConfigRepository extends JpaRepository<AiPromptConfigEntity, Long>, JpaSpecificationExecutor<AiPromptConfigEntity> {

    /**
     * 根据提示词key查询
     */
    Optional<AiPromptConfigEntity> findByPromptKey(String promptKey);

    /**
     * 根据提示词key查询（忽略软删除）
     */
    Optional<AiPromptConfigEntity> findByPromptKeyAndDelFlag(String promptKey, Integer delFlag);

    /**
     * 判断提示词key是否存在
     */
    boolean existsByPromptKey(String promptKey);

    /**
     * 判断提示词key是否存在（排除指定ID）
     */
    boolean existsByPromptKeyAndIdNot(String promptKey, Long id);
}
