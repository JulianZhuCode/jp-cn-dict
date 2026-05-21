package io.github.jpcndict.dto;

import lombok.Data;

/**
 * 例句数据传输对象
 */
@Data
public class ExamplesDTO {
    private Integer id;
    private String jp;
    private String cn;
}
