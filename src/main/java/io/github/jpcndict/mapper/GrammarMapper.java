package io.github.jpcndict.mapper;

import io.github.jpcndict.dto.GrammarDTO;
import io.github.jpcndict.entity.GrammarEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 语法实体与DTO转换器（MapStruct）
 */
@Mapper(componentModel = "spring")
public interface GrammarMapper {

    GrammarMapper INSTANCE = Mappers.getMapper(GrammarMapper.class);

    /**
     * Entity → DTO
     */
    GrammarDTO toDTO(GrammarEntity entity);

    /**
     * DTO → Entity
     */
    GrammarEntity toEntity(GrammarDTO dto);

    /**
     * Entity列表 → DTO列表
     */
    List<GrammarDTO> toDTOList(List<GrammarEntity> entities);
}
