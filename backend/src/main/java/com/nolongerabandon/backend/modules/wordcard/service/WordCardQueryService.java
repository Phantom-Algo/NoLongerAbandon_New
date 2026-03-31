package com.nolongerabandon.backend.modules.wordcard.service;

import com.nolongerabandon.backend.modules.wordcard.dto.ChineseLookupRequest;
import com.nolongerabandon.backend.modules.wordcard.dto.SectionRegenerateRequest;
import com.nolongerabandon.backend.modules.wordcard.dto.WordCardRegenerateRequest;
import com.nolongerabandon.backend.modules.wordcard.dto.WordLookupRequest;
import com.nolongerabandon.backend.modules.wordcard.dto.WordSearchRequest;
import com.nolongerabandon.backend.modules.wordcard.vo.ChineseLookupVO;
import com.nolongerabandon.backend.modules.wordcard.vo.WordCardBriefVO;
import com.nolongerabandon.backend.modules.wordcard.vo.WordCardVO;

public interface WordCardQueryService {

    /**
     * 预览查询（轻量级，仅检查缓存状态）
     */
    WordCardBriefVO previewLookup(WordLookupRequest request);

    /**
     * 英文单词搜索（命中缓存直接返回，未命中调用 AI 生成）
     */
    WordCardVO search(WordSearchRequest request);

    /**
     * 获取单词卡详情
     */
    WordCardVO getWordCard(Long wordId);

    /**
     * 整卡重新生成
     */
    WordCardVO regenerateWordCard(Long wordId, WordCardRegenerateRequest request);

    /**
     * 单模块重新生成
     */
    WordCardVO regenerateSection(Long wordId, Long sectionId, SectionRegenerateRequest request);

    /**
     * 中文搜索候选词
     */
    ChineseLookupVO chineseLookup(ChineseLookupRequest request);
}