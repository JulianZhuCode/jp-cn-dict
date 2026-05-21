package io.github.jpcndict.dto;

import lombok.Data;

/**
 * 单词数据传输对象
 */
@Data
public class WordDTO {
    private Integer id;
    private String word;
    private String reading;
    private String romaji;
    private String[] meaning;
    private String[] notes;
    private String pos;
}
