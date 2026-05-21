package io.github.jpcndict.mapper;

import io.github.jpcndict.dto.WordDTO;
import io.github.jpcndict.entity.WordEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 单词实体与DTO转换器（MapStruct）
 */
@Mapper(componentModel = "spring")
public interface WordMapper {

    WordMapper INSTANCE = Mappers.getMapper(WordMapper.class);

    /**
     * Entity → DTO
     */
    WordDTO toDTO(WordEntity entity);

    /**
     * DTO → Entity
     */
    WordEntity toEntity(WordDTO dto);

    /**
     * Entity列表 → DTO列表
     */
    List<WordDTO> toDTOList(List<WordEntity> entities);
}
