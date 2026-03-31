package com.nolongerabandon.backend.modules.wordcard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nolongerabandon.backend.common.exception.BusinessException;
import com.nolongerabandon.backend.modules.wordcard.dto.CustomSectionCreateRequest;
import com.nolongerabandon.backend.modules.wordcard.dto.CustomSectionUpdateRequest;
import com.nolongerabandon.backend.modules.wordcard.entity.CustomSectionTemplate;
import com.nolongerabandon.backend.modules.wordcard.mapper.CustomSectionTemplateMapper;
import com.nolongerabandon.backend.modules.wordcard.service.CustomSectionTemplateService;
import com.nolongerabandon.backend.modules.wordcard.vo.CustomSectionTemplateVO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CustomSectionTemplateServiceImpl implements CustomSectionTemplateService {

    private final CustomSectionTemplateMapper templateMapper;

    public CustomSectionTemplateServiceImpl(CustomSectionTemplateMapper templateMapper) {
        this.templateMapper = templateMapper;
    }

    @Override
    public List<CustomSectionTemplateVO> listAll() {
        LambdaQueryWrapper<CustomSectionTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(CustomSectionTemplate::getId);
        return templateMapper.selectList(wrapper)
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public CustomSectionTemplateVO create(CustomSectionCreateRequest request) {
        CustomSectionTemplate entity = new CustomSectionTemplate();
        entity.setTitle(request.getTitle().trim());
        entity.setPrompt(request.getPrompt().trim());
        templateMapper.insert(entity);
        return toVO(templateMapper.selectById(entity.getId()));
    }

    @Override
    public CustomSectionTemplateVO update(Long id, CustomSectionUpdateRequest request) {
        CustomSectionTemplate entity = requireTemplate(id);
        entity.setTitle(request.getTitle().trim());
        entity.setPrompt(request.getPrompt().trim());
        templateMapper.updateById(entity);
        return toVO(templateMapper.selectById(id));
    }

    @Override
    public void delete(Long id) {
        requireTemplate(id);
        templateMapper.deleteById(id);
    }

    private CustomSectionTemplate requireTemplate(Long id) {
        CustomSectionTemplate entity = templateMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("CUSTOM_SECTION_NOT_FOUND", "自定义模块模板不存在");
        }
        return entity;
    }

    private CustomSectionTemplateVO toVO(CustomSectionTemplate entity) {
        return CustomSectionTemplateVO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .prompt(entity.getPrompt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
