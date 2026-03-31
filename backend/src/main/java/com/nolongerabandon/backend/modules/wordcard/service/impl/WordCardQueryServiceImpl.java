package com.nolongerabandon.backend.modules.wordcard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nolongerabandon.backend.modules.wordcard.dto.WordLookupRequest;
import com.nolongerabandon.backend.modules.wordcard.entity.Word;
import com.nolongerabandon.backend.modules.wordcard.mapper.WordMapper;
import com.nolongerabandon.backend.modules.wordcard.service.WordCardQueryService;
import com.nolongerabandon.backend.modules.wordcard.vo.WordCardBriefVO;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class WordCardQueryServiceImpl implements WordCardQueryService {

    private final WordMapper wordMapper;

    public WordCardQueryServiceImpl(WordMapper wordMapper) {
        this.wordMapper = wordMapper;
    }

    @Override
    public WordCardBriefVO previewLookup(WordLookupRequest request) {
        String normalizedWord = request.getWord().trim().toLowerCase(Locale.ROOT);
        QueryWrapper<Word> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("word", normalizedWord);
        Word existingWord = wordMapper.selectOne(queryWrapper);

        if (existingWord != null) {
            return WordCardBriefVO.builder()
                    .word(normalizedWord)
                    .cached(true)
                    .searchCount(existingWord.getSearchCount())
                    .nextAction("已命中本地缓存，后续可继续接入完整单词卡查询与展示流程。")
                    .build();
        }

        return WordCardBriefVO.builder()
                .word(normalizedWord)
                .cached(false)
                .searchCount(0)
                .nextAction("当前未命中缓存，后续将转入 AI 工作流生成单词卡。")
                .build();
    }
}