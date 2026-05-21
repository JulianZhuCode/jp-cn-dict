package io.github.jpcndict.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "grammar", schema = "dict", indexes = {
        @Index(name = "idx_grammar_word", columnList = "word"),
        @Index(name = "idx_grammar_reading", columnList = "reading")
})
@Data
public class GrammarEntity {
    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_grammar_id")
    @SequenceGenerator(name = "seq_grammar_id", sequenceName = "seq_grammar_id", allocationSize = 1)
    private Integer id;
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
    private String[] meaning;
    /**
     * 说明
     */
    private String[] notes;
}
