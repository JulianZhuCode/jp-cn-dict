package io.github.jpcndict.mapper;

import io.github.jpcndict.dto.vo.GrammarVO;
import io.github.jpcndict.entity.GrammarEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 语法实体与VO转换器（MapStruct）
 */
@Mapper(componentModel = "spring")
public interface GrammarMapper {

    GrammarMapper INSTANCE = Mappers.getMapper(GrammarMapper.class);

    /**
     * Entity → DTO
     */
    GrammarVO toVO(GrammarEntity entity);

    /**
     * DTO → Entity
     */
    GrammarEntity toEntity(GrammarVO vo);

    /**
     * Entity列表 → DTO列表
     */
    List<GrammarVO> toVOList(List<GrammarEntity> entities);
}
