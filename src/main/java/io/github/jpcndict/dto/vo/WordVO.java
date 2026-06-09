package io.github.jpcndict.dto.vo;

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
    private String[] notes;
    private String pos;
}
