package io.github.jpcndict.dao.entity;

import io.github.springwhale.database.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ai_prompt_config", schema = "dict")
@Data
public class AiPromptConfigEntity extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String promptKey;

    @Column(nullable = false)
    private String promptName;

    @Column(nullable = false)
    private String systemPrompt;

    private String userPromptTemplate;

    private String modelName;

    @Column(nullable = false)
    private Boolean enabled = true;
}