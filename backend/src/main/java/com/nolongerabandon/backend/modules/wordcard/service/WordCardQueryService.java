package com.nolongerabandon.backend.modules.wordcard.service;

import com.nolongerabandon.backend.modules.wordcard.dto.WordLookupRequest;
import com.nolongerabandon.backend.modules.wordcard.vo.WordCardBriefVO;

public interface WordCardQueryService {

    WordCardBriefVO previewLookup(WordLookupRequest request);
}