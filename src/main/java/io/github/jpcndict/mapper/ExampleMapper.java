package io.github.jpcndict.mapper;

import io.github.jpcndict.dto.vo.ExampleVO;
import io.github.jpcndict.entity.ExampleEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 例句实体与VO转换器（MapStruct）
 */
@Mapper(componentModel = "spring")
public interface ExampleMapper {

    /**
     * Entity → VO
     */
    ExampleVO toVO(ExampleEntity entity);

    /**
     * VO → Entity
     */
    ExampleEntity toEntity(ExampleVO vo);

    /**
     * Entity列表 → VO列表
     */
    List<ExampleVO> toVOList(List<ExampleEntity> entities);
}