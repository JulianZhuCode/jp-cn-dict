package io.github.jpcndict.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 语法创建/更新请求
 */
@Data
public class GrammarRequest {

    @NotBlank(message = "语法条目不能为空")
    private String pattern;

    private String reading;
    private String[] meaning;
}
