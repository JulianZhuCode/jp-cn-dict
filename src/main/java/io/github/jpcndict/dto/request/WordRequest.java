package io.github.jpcndict.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 单词创建/更新请求
 */
@Data
public class WordRequest {

    @NotBlank(message = "单词不能为空")
    private String word;

    private String reading;
    private String romaji;
    private String[] meaning;
    private String[] notes;
    private String pos;
}
