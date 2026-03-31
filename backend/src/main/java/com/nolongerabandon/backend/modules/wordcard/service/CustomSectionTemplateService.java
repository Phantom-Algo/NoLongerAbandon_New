package com.nolongerabandon.backend.modules.wordcard.service;

import com.nolongerabandon.backend.modules.wordcard.dto.CustomSectionCreateRequest;
import com.nolongerabandon.backend.modules.wordcard.dto.CustomSectionUpdateRequest;
import com.nolongerabandon.backend.modules.wordcard.vo.CustomSectionTemplateVO;
import java.util.List;

public interface CustomSectionTemplateService {

    List<CustomSectionTemplateVO> listAll();

    CustomSectionTemplateVO create(CustomSectionCreateRequest request);

    CustomSectionTemplateVO update(Long id, CustomSectionUpdateRequest request);

    void delete(Long id);
}
