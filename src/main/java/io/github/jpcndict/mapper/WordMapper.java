package io.github.jpcndict.mapper;

import io.github.jpcndict.dao.entity.WordEntity;
import io.github.jpcndict.dto.vo.WordVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 单词实体与VO转换器
 */
@Component
public class WordMapper {

    /**
     * Entity → VO
     */
    public WordVO toVO(WordEntity entity) {
        if (entity == null) {
            return null;
        }
        WordVO vo = new WordVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * VO → Entity
     */
    public WordEntity toEntity(WordVO vo) {
        if (vo == null) {
            return null;
        }
        WordEntity entity = new WordEntity();
        BeanUtils.copyProperties(vo, entity);
        return entity;
    }

    /**
     * Entity列表 → VO列表
     */
    public List<WordVO> toVOList(List<WordEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }
}
