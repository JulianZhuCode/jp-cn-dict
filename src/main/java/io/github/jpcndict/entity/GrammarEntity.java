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
@Table(name = "grammar", schema = "dict", indexes = {
        @Index(name = "idx_grammar_word", columnList = "word"),
        @Index(name = "idx_grammar_reading", columnList = "reading")
})
@Data
public class GrammarEntity extends BaseEntity {
    /**
     * 语法条目
     */
    private String word;
    /**
     * 读音
     */
    private String reading;
    /**
     * 意义
     */
    @Column(columnDefinition = "_text")
    private String[] meaning;
    /**
     * 说明
     */
    @Column(columnDefinition = "_text")
    private String[] notes;
    /**
     * 是否人工确认
     */
    private Boolean isManualConfirmed;
}
