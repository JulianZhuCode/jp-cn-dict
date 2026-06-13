package io.github.jpcndict.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 例句创建/更新请求
 */
@Data
public class ExamplesRequest {

    @NotBlank(message = "日语例句不能为空")
    private String jp;

    @NotBlank(message = "中文翻译不能为空")
    private String cn;
}
