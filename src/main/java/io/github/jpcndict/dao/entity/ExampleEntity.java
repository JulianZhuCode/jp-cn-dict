package io.github.jpcndict.dao.entity;

import io.github.springwhale.database.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "example", schema = "dict")
@Data
public class ExampleEntity extends BaseEntity {
    private String jp;
    private String cn;
    private Integer[] relatedWords;
    private Integer[] relatedGrammars;
    private String audioUrl;
}