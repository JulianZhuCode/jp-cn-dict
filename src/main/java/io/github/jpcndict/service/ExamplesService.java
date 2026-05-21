package io.github.jpcndict.service;

import io.github.jpcndict.dto.ExamplesDTO;
import io.github.jpcndict.entity.ExamplesEntity;
import io.github.jpcndict.mapper.ExamplesMapper;
import io.github.jpcndict.repository.ExamplesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamplesService {

    private final ExamplesRepository examplesRepository;
    private final ExamplesMapper examplesMapper;

    /**
     * 分页查询所有例句
     */
    public Page<ExamplesDTO> findAll(Pageable pageable) {
        return examplesRepository.findAll(pageable).map(examplesMapper::toDTO);
    }

    /**
     * 根据ID查询例句
     */
    public Optional<ExamplesDTO> findById(Integer id) {
        return examplesRepository.findById(id).map(examplesMapper::toDTO);
    }

    /**
     * 搜索例句（支持日语或中文模糊查询）
     */
    public List<ExamplesDTO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return examplesMapper.toDTOList(examplesRepository.findByJpContainingOrCnContaining(keyword, keyword));
    }

    /**
     * 创建例句
     */
    @Transactional
    public ExamplesDTO create(ExamplesDTO exampleDTO) {
        ExamplesEntity entity = examplesMapper.toEntity(exampleDTO);
        return examplesMapper.toDTO(examplesRepository.save(entity));
    }

    /**
     * 更新例句
     */
    @Transactional
    public ExamplesDTO update(Integer id, ExamplesDTO exampleDTO) {
        ExamplesEntity example = examplesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("例句不存在，ID: " + id));

        example.setJp(exampleDTO.getJp());
        example.setCn(exampleDTO.getCn());

        return examplesMapper.toDTO(examplesRepository.save(example));
    }

    /**
     * 删除例句
     */
    @Transactional
    public void delete(Integer id) {
        ExamplesEntity example = examplesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("例句不存在，ID: " + id));
        examplesRepository.delete(example);
    }
}
