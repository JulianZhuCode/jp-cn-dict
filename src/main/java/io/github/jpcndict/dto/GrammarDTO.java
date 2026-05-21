package io.github.jpcndict.dto;

import lombok.Data;

/**
 * 语法数据传输对象
 */
@Data
public class GrammarDTO {
    private Integer id;
    private String word;
    private String reading;
    private String[] meaning;
    private String[] notes;
}
