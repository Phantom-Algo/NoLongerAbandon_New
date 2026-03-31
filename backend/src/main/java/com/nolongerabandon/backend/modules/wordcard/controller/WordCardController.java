package com.nolongerabandon.backend.modules.wordcard.controller;

import com.nolongerabandon.backend.common.api.ApiResponse;
import com.nolongerabandon.backend.modules.wordcard.dto.ChineseLookupRequest;
import com.nolongerabandon.backend.modules.wordcard.dto.CustomSectionCreateRequest;
import com.nolongerabandon.backend.modules.wordcard.dto.CustomSectionUpdateRequest;
import com.nolongerabandon.backend.modules.wordcard.dto.SectionRegenerateRequest;
import com.nolongerabandon.backend.modules.wordcard.dto.WordCardRegenerateRequest;
import com.nolongerabandon.backend.modules.wordcard.dto.WordLookupRequest;
import com.nolongerabandon.backend.modules.wordcard.dto.WordSearchRequest;
import com.nolongerabandon.backend.modules.wordcard.service.CustomSectionTemplateService;
import com.nolongerabandon.backend.modules.wordcard.service.WordCardQueryService;
import com.nolongerabandon.backend.modules.wordcard.vo.ChineseLookupVO;
import com.nolongerabandon.backend.modules.wordcard.vo.CustomSectionTemplateVO;
import com.nolongerabandon.backend.modules.wordcard.vo.WordCardBriefVO;
import com.nolongerabandon.backend.modules.wordcard.vo.WordCardVO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/word-cards")
public class WordCardController {

    private final WordCardQueryService wordCardQueryService;
    private final CustomSectionTemplateService customSectionTemplateService;

    public WordCardController(WordCardQueryService wordCardQueryService,
                              CustomSectionTemplateService customSectionTemplateService) {
        this.wordCardQueryService = wordCardQueryService;
        this.customSectionTemplateService = customSectionTemplateService;
    }

    // ==================== 单词搜索与单词卡 ====================

    @PostMapping("/preview")
    public ApiResponse<WordCardBriefVO> preview(@Valid @RequestBody WordLookupRequest request) {
        return ApiResponse.success(wordCardQueryService.previewLookup(request));
    }

    @PostMapping("/search")
    public ApiResponse<WordCardVO> search(@Valid @RequestBody WordSearchRequest request) {
        return ApiResponse.success(wordCardQueryService.search(request));
    }

    @GetMapping("/{wordId}")
    public ApiResponse<WordCardVO> getWordCard(@PathVariable Long wordId) {
        return ApiResponse.success(wordCardQueryService.getWordCard(wordId));
    }

    @PostMapping("/{wordId}/regenerate")
    public ApiResponse<WordCardVO> regenerate(@PathVariable Long wordId,
                                              @RequestBody(required = false) WordCardRegenerateRequest request) {
        return ApiResponse.success(wordCardQueryService.regenerateWordCard(wordId, request));
    }

    @PostMapping("/{wordId}/sections/{sectionId}/regenerate")
    public ApiResponse<WordCardVO> regenerateSection(@PathVariable Long wordId,
                                                     @PathVariable Long sectionId,
                                                     @RequestBody(required = false) SectionRegenerateRequest request) {
        return ApiResponse.success(wordCardQueryService.regenerateSection(wordId, sectionId, request));
    }

    @PostMapping("/chinese-lookup")
    public ApiResponse<ChineseLookupVO> chineseLookup(@Valid @RequestBody ChineseLookupRequest request) {
        return ApiResponse.success(wordCardQueryService.chineseLookup(request));
    }

    // ==================== 自定义模块模板 CRUD ====================

    @GetMapping("/custom-sections")
    public ApiResponse<List<CustomSectionTemplateVO>> listCustomSections() {
        return ApiResponse.success(customSectionTemplateService.listAll());
    }

    @PostMapping("/custom-sections")
    public ApiResponse<CustomSectionTemplateVO> createCustomSection(
            @Valid @RequestBody CustomSectionCreateRequest request) {
        return ApiResponse.success(customSectionTemplateService.create(request));
    }

    @PutMapping("/custom-sections/{id}")
    public ApiResponse<CustomSectionTemplateVO> updateCustomSection(
            @PathVariable Long id,
            @Valid @RequestBody CustomSectionUpdateRequest request) {
        return ApiResponse.success(customSectionTemplateService.update(id, request));
    }

    @DeleteMapping("/custom-sections/{id}")
    public ApiResponse<Void> deleteCustomSection(@PathVariable Long id) {
        customSectionTemplateService.delete(id);
        return ApiResponse.success(null);
    }
}