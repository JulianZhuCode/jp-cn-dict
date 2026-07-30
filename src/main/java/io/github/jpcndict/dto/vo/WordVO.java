package io.github.jpcndict.dto.vo;

import io.github.jpcndict.enums.WordPos;
import lombok.Data;

/**
 * 单词视图对象
 */
@Data
public class WordVO {
    private Integer id;
    private String word;
    private String reading;
    private String romaji;
    private String[] meaning;
    private String pos;
    private String audioUrl;

    public String getPosDescription() {
        if (pos == null) return null;
        try {
            return WordPos.valueOf(pos).getRemark();
        } catch (IllegalArgumentException e) {
            return pos;
        }
    }
}
