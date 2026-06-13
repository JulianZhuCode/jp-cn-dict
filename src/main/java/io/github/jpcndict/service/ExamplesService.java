package io.github.jpcndict.service;

import io.github.jpcndict.dto.request.ExamplesRequest;
import io.github.jpcndict.dto.vo.ExamplesVO;
import io.github.jpcndict.entity.ExamplesEntity;
import io.github.jpcndict.mapper.ExamplesMapper;
import io.github.jpcndict.repository.ExamplesRepository;
import io.github.springwhale.framework.core.exception.BusinessException;
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
    public Page<ExamplesVO> findAll(Pageable pageable) {
        return examplesRepository.findAll(pageable).map(examplesMapper::toVO);
    }

    /**
     * 根据ID查询例句
     */
    public Optional<ExamplesVO> findById(Integer id) {
        return examplesRepository.findById(id).map(examplesMapper::toVO);
    }

    /**
     * 搜索例句（支持日语或中文模糊查询）
     */
    public List<ExamplesVO> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return examplesMapper.toVOList(examplesRepository.findByJpContainingOrCnContaining(keyword, keyword));
    }

    /**
     * 创建例句
     */
    @Transactional
    public ExamplesVO create(ExamplesRequest request) {
        ExamplesEntity entity = new ExamplesEntity();
        entity.setJp(request.getJp());
        entity.setCn(request.getCn());
        return examplesMapper.toVO(examplesRepository.save(entity));
    }

    /**
     * 更新例句
     */
    @Transactional
    public ExamplesVO update(Integer id, ExamplesRequest request) {
        ExamplesEntity example = examplesRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("EXAMPLE_NOT_FOUND", "例句不存在，ID: " + id));

        example.setJp(request.getJp());
        example.setCn(request.getCn());

        return examplesMapper.toVO(examplesRepository.save(example));
    }

    /**
     * 删除例句
     */
    @Transactional
    public void delete(Integer id) {
        ExamplesEntity example = examplesRepository.findById(id)
                .orElseThrow(() -> BusinessException.create("EXAMPLE_NOT_FOUND", "例句不存在，ID: " + id));
        examplesRepository.delete(example);
    }
}
