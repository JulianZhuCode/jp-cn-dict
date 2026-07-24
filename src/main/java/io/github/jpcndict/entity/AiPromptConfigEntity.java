package io.github.jpcndict.entity;

import io.github.springwhale.database.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ai_prompt_config", schema = "dict", indexes = {
        @Index(name = "idx_ai_prompt_key", columnList = "prompt_key")
})
@Data
public class AiPromptConfigEntity extends BaseEntity {
    /**
     * 提示词唯一标识（如 example_analysis）
     */
    @Column(name = "prompt_key", nullable = false, unique = true, length = 100)
    private String promptKey;

    /**
     * 提示词名称（用于展示）
     */
    @Column(name = "prompt_name", nullable = false, length = 200)
    private String promptName;

    /**
     * 系统提示词（System Prompt）
     */
    @Column(name = "system_prompt", nullable = false, columnDefinition = "text")
    private String systemPrompt;

    /**
     * 用户提示词模板（支持变量替换）
     */
    @Column(name = "user_prompt_template", columnDefinition = "text")
    private String userPromptTemplate;

    /**
     * 使用的模型名称
     */
    @Column(name = "model_name", length = 100)
    private String modelName;

    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled = true;
}
