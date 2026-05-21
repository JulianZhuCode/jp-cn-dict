package io.github.jpcndict.entity;

import io.github.jpcndict.enums.WordPos;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "word", schema = "dict", indexes = {
        @Index(name = "idx_word_word", columnList = "word"),
        @Index(name = "idx_word_reading", columnList = "reading"),
        @Index(name = "idx_word_romaji", columnList = "romaji")
})
@Data
public class WordEntity {
    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_word_id")
    @SequenceGenerator(name = "seq_word_id", sequenceName = "seq_word_id", allocationSize = 1)
    private Integer id;
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

    public WordPos getPosEnum() {
        return pos != null ? WordPos.valueOf(pos) : null;
    }

    public void setPosEnum(WordPos posEnum) {
        this.pos = posEnum != null ? posEnum.name() : null;
    }
}
