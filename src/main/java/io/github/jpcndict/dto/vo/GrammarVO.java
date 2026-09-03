package io.github.jpcndict.dto.vo;

import lombok.Data;

/**
 * 语法视图对象
 */
@Data
public class GrammarVO {
    private Long id;
    private String pattern;
    private String reading;
    private String[] meaning;
}
