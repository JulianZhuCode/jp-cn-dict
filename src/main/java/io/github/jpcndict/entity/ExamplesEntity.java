package io.github.jpcndict.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "examples", schema = "dict")
@Data
public class ExamplesEntity {
    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_examples_id")
    @SequenceGenerator(name = "seq_examples_id", sequenceName = "seq_examples_id", allocationSize = 1)
    private Integer id;
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
}
