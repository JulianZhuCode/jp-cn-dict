package io.github.jpcndict.entity;

import io.github.springwhale.database.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "examples", schema = "dict")
@Data
public class ExamplesEntity extends BaseEntity {
    /**
     * 日语例句
     */
    @Column(columnDefinition = "text")
    private String jp;
    /**
     * 中文翻译
     */
    @Column(columnDefinition = "text")
    private String cn;
    /**
     * 关联单词ID
     */
    @Column(columnDefinition = "integer[]")
    private Integer[] relatedWords;
    /**
     * 关联语法ID
     */
    @Column(columnDefinition = "integer[]")
    private Integer[] relatedGrammars;
    /**
     * 是否人工确认
     */
    private Boolean isManualConfirmed;
}
