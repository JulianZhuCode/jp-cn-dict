package io.github.jpcndict.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

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
}
