package io.github.jpcndict.dao.entity;

import io.github.springwhale.database.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "grammar", schema = "dict")
@Data
public class GrammarEntity extends BaseEntity {
    private String pattern;
    private String reading;
    private String[] meaning;
}