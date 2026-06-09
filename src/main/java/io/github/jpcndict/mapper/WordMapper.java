package io.github.jpcndict.mapper;

import io.github.jpcndict.dto.vo.WordVO;
import io.github.jpcndict.entity.WordEntity;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 单词实体与VO转换器（MapStruct）
 */
@Mapper(componentModel = "spring")
public interface WordMapper {

    /**
     * Entity → VO
     */
    WordVO toVO(WordEntity entity);

    /**
     * VO → Entity
     */
    WordEntity toEntity(WordVO vo);

    /**
     * Entity列表 → VO列表
     */
    List<WordVO> toVOList(List<WordEntity> entities);
}
