package io.github.jpcndict.mapper;

import io.github.jpcndict.dto.vo.GrammarVO;
import io.github.jpcndict.entity.GrammarEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 语法实体与VO转换器（MapStruct）
 */
@Mapper(componentModel = "spring")
public interface GrammarMapper {

    GrammarVO toVO(GrammarEntity entity);

    /**
     * VO → Entity
     */
    GrammarEntity toEntity(GrammarVO vo);

    /**
     * Entity列表 → VO列表
     */
    List<GrammarVO> toVOList(List<GrammarEntity> entities);
}
