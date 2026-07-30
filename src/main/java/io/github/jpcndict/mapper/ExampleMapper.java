package io.github.jpcndict.mapper;

import io.github.jpcndict.dto.vo.ExampleVO;
import io.github.jpcndict.entity.ExampleEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 例句实体与VO转换器
 */
@Component
public class ExampleMapper {

    /**
     * Entity → VO
     */
    public ExampleVO toVO(ExampleEntity entity) {
        if (entity == null) {
            return null;
        }
        ExampleVO vo = new ExampleVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * VO → Entity
     */
    public ExampleEntity toEntity(ExampleVO vo) {
        if (vo == null) {
            return null;
        }
        ExampleEntity entity = new ExampleEntity();
        BeanUtils.copyProperties(vo, entity);
        return entity;
    }

    /**
     * Entity列表 → VO列表
     */
    public List<ExampleVO> toVOList(List<ExampleEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }
}
