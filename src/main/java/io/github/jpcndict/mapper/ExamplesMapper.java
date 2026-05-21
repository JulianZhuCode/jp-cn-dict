package io.github.jpcndict.mapper;

import io.github.jpcndict.dto.ExamplesDTO;
import io.github.jpcndict.entity.ExamplesEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 例句实体与DTO转换器（MapStruct）
 */
@Mapper(componentModel = "spring")
public interface ExamplesMapper {

    ExamplesMapper INSTANCE = Mappers.getMapper(ExamplesMapper.class);

    /**
     * Entity → DTO
     */
    ExamplesDTO toDTO(ExamplesEntity entity);

    /**
     * DTO → Entity
     */
    ExamplesEntity toEntity(ExamplesDTO dto);

    /**
     * Entity列表 → DTO列表
     */
    List<ExamplesDTO> toDTOList(List<ExamplesEntity> entities);
}
