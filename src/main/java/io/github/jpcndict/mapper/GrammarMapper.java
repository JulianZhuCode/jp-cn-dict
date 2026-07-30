package io.github.jpcndict.mapper;

import io.github.jpcndict.dto.vo.GrammarVO;
import io.github.jpcndict.entity.GrammarEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 语法实体与VO转换器
 */
@Component
public class GrammarMapper {

    /**
     * Entity → VO
     */
    public GrammarVO toVO(GrammarEntity entity) {
        if (entity == null) {
            return null;
        }
        GrammarVO vo = new GrammarVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * VO → Entity
     */
    public GrammarEntity toEntity(GrammarVO vo) {
        if (vo == null) {
            return null;
        }
        GrammarEntity entity = new GrammarEntity();
        BeanUtils.copyProperties(vo, entity);
        return entity;
    }

    /**
     * Entity列表 → VO列表
     */
    public List<GrammarVO> toVOList(List<GrammarEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }
}
