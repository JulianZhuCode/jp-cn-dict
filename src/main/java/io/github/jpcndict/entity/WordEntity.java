package io.github.jpcndict.entity;

import io.github.jpcndict.enums.WordPos;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

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
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
    /**
     * 创建人
     */
    private String createBy;
    /**
     * 修改人
     */
    private String updateBy;
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
