package io.github.jpcndict.mapper;

import io.github.jpcndict.dto.vo.ExamplesVO;
import io.github.jpcndict.entity.ExamplesEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 例句实体与VO转换器（MapStruct）
 */
@Mapper(componentModel = "spring")
public interface ExamplesMapper {

    /**
     * Entity → VO
     */
    ExamplesVO toVO(ExamplesEntity entity);

    /**
     * VO → Entity
     */
    ExamplesEntity toEntity(ExamplesVO vo);

    /**
     * Entity列表 → VO列表
     */
    List<ExamplesVO> toVOList(List<ExamplesEntity> entities);
}
