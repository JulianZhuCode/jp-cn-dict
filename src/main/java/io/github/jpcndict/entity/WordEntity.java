package io.github.jpcndict.entity;

import io.github.jpcndict.enums.WordPos;
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
    private String[] meaning;
    /**
     * 说明
     */
    private String[] notes;
    /**
     * 词性
     */
    @Column(length = 50)
    private String pos;
    /**
     * 是否人工确认
     */
    private Boolean isManualConfirmed;

    public WordPos getPosEnum() {
        return pos != null ? WordPos.valueOf(pos) : null;
    }

    public void setPosEnum(WordPos posEnum) {
        this.pos = posEnum != null ? posEnum.name() : null;
    }
}
