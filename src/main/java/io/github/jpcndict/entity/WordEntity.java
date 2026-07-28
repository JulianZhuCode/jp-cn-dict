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
@Table(name = "word", schema = "dict", indexes = {
        @Index(name = "idx_word_word", columnList = "word"),
        @Index(name = "idx_word_reading", columnList = "reading"),
        @Index(name = "idx_word_romaji", columnList = "romaji")
})
@Data
public class WordEntity extends BaseEntity {
    /**
     * 单词
     */
    private String word;
    /**
     * 读音
     */
    private String reading;
    /**
     * 罗马音
     */
    private String romaji;
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
     * 词性
     */
    @Column(length = 50)
    private String pos;
}
