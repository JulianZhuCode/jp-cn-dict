package io.github.jpcndict.dao.entity;

import io.github.jpcndict.util.KanaRomajiUtil;
import io.github.springwhale.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "word", schema = "dict")
@Data
public class WordEntity extends BaseEntity {
    private String word;
    private String reading;
    @Transient
    private String romaji;
    private String[] meaning;
    private String pos;
    private String audioUrl;

    /**
     * 动态根据假名（reading）生成罗马音；无假名时返回 null。
     */
    public String getRomaji() {
        return KanaRomajiUtil.toRomaji(this.reading);
    }
}