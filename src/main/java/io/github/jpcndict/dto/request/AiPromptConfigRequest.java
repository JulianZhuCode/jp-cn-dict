package io.github.jpcndict.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiPromptConfigRequest {

    @NotBlank(message = "提示词标识不能为空")
    @Size(max = 100, message = "提示词标识长度不能超过100")
    private String promptKey;

    @NotBlank(message = "提示词名称不能为空")
    @Size(max = 200, message = "提示词名称长度不能超过200")
    private String promptName;

    @NotBlank(message = "系统提示词不能为空")
    private String systemPrompt;

    private String userPromptTemplate;

    @Size(max = 100, message = "模型名称长度不能超过100")
    private String modelName;

    private Boolean enabled = true;
}
