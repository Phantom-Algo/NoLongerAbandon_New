package com.nolongerabandon.backend.modules.wordcard.controller;

import com.nolongerabandon.backend.common.api.ApiResponse;
import com.nolongerabandon.backend.modules.wordcard.dto.WordLookupRequest;
import com.nolongerabandon.backend.modules.wordcard.service.WordCardQueryService;
import com.nolongerabandon.backend.modules.wordcard.vo.WordCardBriefVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/word-cards")
public class WordCardController {

    private final WordCardQueryService wordCardQueryService;

    public WordCardController(WordCardQueryService wordCardQueryService) {
        this.wordCardQueryService = wordCardQueryService;
    }

    @PostMapping("/preview")
    public ApiResponse<WordCardBriefVO> preview(@Valid @RequestBody WordLookupRequest request) {
        return ApiResponse.success(wordCardQueryService.previewLookup(request));
    }
}