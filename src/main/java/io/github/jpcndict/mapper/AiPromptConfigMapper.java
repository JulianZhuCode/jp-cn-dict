package io.github.jpcndict.mapper;

import io.github.jpcndict.dto.request.AiPromptConfigRequest;
import io.github.jpcndict.dto.vo.AiPromptConfigVO;
import io.github.jpcndict.entity.AiPromptConfigEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI提示词配置实体与VO转换器
 */
@Component
public class AiPromptConfigMapper {

    /**
     * Entity → VO
     */
    public AiPromptConfigVO toVO(AiPromptConfigEntity entity) {
        if (entity == null) {
            return null;
        }
        AiPromptConfigVO vo = new AiPromptConfigVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * Entity列表 → VO列表
     */
    public List<AiPromptConfigVO> toVOList(List<AiPromptConfigEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * Request → Entity (更新现有实体)
     */
    public void updateFromRequest(AiPromptConfigRequest request, AiPromptConfigEntity entity) {
        if (request == null || entity == null) {
            return;
        }
        BeanUtils.copyProperties(request, entity);
    }
}
