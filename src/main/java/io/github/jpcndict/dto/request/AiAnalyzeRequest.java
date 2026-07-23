package io.github.jpcndict.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI分析例句请求
 */
@Data
public class AiAnalyzeRequest {

    @NotBlank(message = "日语例句不能为空")
    private String jp;

}