package io.github.jpcndict.mapper;

import io.github.jpcndict.dto.request.AiPromptConfigRequest;
import io.github.jpcndict.dto.vo.AiPromptConfigVO;
import io.github.jpcndict.entity.AiPromptConfigEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AiPromptConfigMapper {

    AiPromptConfigVO toVO(AiPromptConfigEntity entity);

    List<AiPromptConfigVO> toVOList(List<AiPromptConfigEntity> entities);

    void updateFromRequest(AiPromptConfigRequest request, @MappingTarget AiPromptConfigEntity entity);
}
