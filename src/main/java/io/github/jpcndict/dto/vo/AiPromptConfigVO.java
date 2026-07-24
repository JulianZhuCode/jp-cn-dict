package io.github.jpcndict.dto.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiPromptConfigVO {

    private Integer id;

    private String promptKey;

    private String promptName;

    private String systemPrompt;

    private String userPromptTemplate;

    private String modelName;

    private Boolean enabled;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
