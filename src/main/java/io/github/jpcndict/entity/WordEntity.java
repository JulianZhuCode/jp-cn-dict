package io.github.jpcndict.entity;

import io.github.jpcndict.util.KanaRomajiUtil;
import io.github.springwhale.database.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "word", schema = "dict", indexes = {
        @Index(name = "idx_word_word", columnList = "word"),
        @Index(name = "idx_word_reading", columnList = "reading")
})
@Data
public class WordEntity extends BaseEntity {
    /**
     * 单词
     */
    private String word;
    /**
     * 读音（假名）
     */
    private String reading;
    /**
     * 罗马音：不存储在数据库中，每次基于 reading 动态生成。
     * <p>
     * 保留此字段（@Transient）的原因：
     * 1. BeanUtils.copyProperties 能直接将值拷贝到 WordVO.romaji
     * 2. DictImportService 导入/导出兼容性（setRomaji 仅写入字段，getRomaji 始终基于 reading 返回，故旧数据中即使有 romaji 也不影响）
     */
    @Transient
    private String romaji;
    /**
     * 意义
     */
    @Column(columnDefinition = "_text")
    private String[] meaning;
    /**
     * 词性
     */
    @Column(length = 50)
    private String pos;
    /**
     * 音频URL
     */
    private String audioUrl;

    /**
     * 动态根据假名（reading）生成罗马音；无假名时返回 null。
     */
    public String getRomaji() {
        return KanaRomajiUtil.toRomaji(this.reading);
    }
}
