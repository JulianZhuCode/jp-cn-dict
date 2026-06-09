package io.github.jpcndict.dto.vo;

import lombok.Data;

/**
 * 语法视图对象
 */
@Data
public class GrammarVO {
    private Integer id;
    private String word;
    private String reading;
    private String[] meaning;
    private String[] notes;
}
