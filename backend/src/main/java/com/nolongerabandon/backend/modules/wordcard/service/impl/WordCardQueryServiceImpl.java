package com.nolongerabandon.backend.modules.wordcard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nolongerabandon.backend.common.exception.BusinessException;
import com.nolongerabandon.backend.modules.settings.entity.ModelConfig;
import com.nolongerabandon.backend.modules.settings.mapper.ModelConfigMapper;
import com.nolongerabandon.backend.modules.wordcard.dto.ChineseLookupRequest;
import com.nolongerabandon.backend.modules.wordcard.dto.SectionRegenerateRequest;
import com.nolongerabandon.backend.modules.wordcard.dto.WordCardRegenerateRequest;
import com.nolongerabandon.backend.modules.wordcard.dto.WordLookupRequest;
import com.nolongerabandon.backend.modules.wordcard.dto.WordSearchRequest;
import com.nolongerabandon.backend.modules.wordcard.entity.CustomSectionTemplate;
import com.nolongerabandon.backend.modules.wordcard.entity.Word;
import com.nolongerabandon.backend.modules.wordcard.entity.WordCard;
import com.nolongerabandon.backend.modules.wordcard.entity.WordCardSection;
import com.nolongerabandon.backend.modules.wordcard.mapper.CustomSectionTemplateMapper;
import com.nolongerabandon.backend.modules.wordcard.mapper.WordCardMapper;
import com.nolongerabandon.backend.modules.wordcard.mapper.WordCardSectionMapper;
import com.nolongerabandon.backend.modules.wordcard.mapper.WordMapper;
import com.nolongerabandon.backend.modules.wordcard.service.WordCardGenerationService;
import com.nolongerabandon.backend.modules.wordcard.service.WordCardQueryService;
import com.nolongerabandon.backend.modules.wordcard.vo.CandidateWordVO;
import com.nolongerabandon.backend.modules.wordcard.vo.ChineseLookupVO;
import com.nolongerabandon.backend.modules.wordcard.vo.SectionVO;
import com.nolongerabandon.backend.modules.wordcard.vo.WordCardBriefVO;
import com.nolongerabandon.backend.modules.wordcard.vo.WordCardDetailVO;
import com.nolongerabandon.backend.modules.wordcard.vo.WordCardVO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class WordCardQueryServiceImpl implements WordCardQueryService {

    /** 预设模块标题列表 */
    private static final List<String> PRESET_SECTION_TITLES = List.of(
            "基本释义", "音标", "例句", "短语", "记背方法"
    );

    private static final Pattern SECTION_PATTERN = Pattern.compile("^##\\s+(.+)$", Pattern.MULTILINE);

    private final WordMapper wordMapper;
    private final WordCardMapper wordCardMapper;
    private final WordCardSectionMapper sectionMapper;
    private final CustomSectionTemplateMapper templateMapper;
    private final ModelConfigMapper modelConfigMapper;
    private final WordCardGenerationService generationService;
    private final ObjectMapper objectMapper;

    public WordCardQueryServiceImpl(WordMapper wordMapper,
                                    WordCardMapper wordCardMapper,
                                    WordCardSectionMapper sectionMapper,
                                    CustomSectionTemplateMapper templateMapper,
                                    ModelConfigMapper modelConfigMapper,
                                    WordCardGenerationService generationService,
                                    ObjectMapper objectMapper) {
        this.wordMapper = wordMapper;
        this.wordCardMapper = wordCardMapper;
        this.sectionMapper = sectionMapper;
        this.templateMapper = templateMapper;
        this.modelConfigMapper = modelConfigMapper;
        this.generationService = generationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public WordCardBriefVO previewLookup(WordLookupRequest request) {
        String normalizedWord = normalize(request.getWord());
        Word existingWord = findWord(normalizedWord);

        if (existingWord != null) {
            WordCard card = findLatestCard(existingWord.getId());
            return WordCardBriefVO.builder()
                    .word(normalizedWord)
                    .cached(card != null)
                    .searchCount(existingWord.getSearchCount())
                    .nextAction(card != null ? "已命中本地缓存，可直接查看单词卡。" : "单词存在但尚无单词卡，将通过 AI 生成。")
                    .build();
        }

        return WordCardBriefVO.builder()
                .word(normalizedWord)
                .cached(false)
                .searchCount(0)
                .nextAction("当前未命中缓存，将通过 AI 生成单词卡。")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WordCardVO search(WordSearchRequest request) {
        String normalizedWord = normalize(request.getWord());
        Word word = findWord(normalizedWord);

        if (word != null) {
            WordCard existingCard = findLatestCard(word.getId());
            if (existingCard != null) {
                // 命中缓存：search_count + 1，直接返回
                incrementSearchCount(word);
                return buildWordCardVO(word, existingCard, true);
            }
        }

        // 未命中缓存：调用AI生成
        List<CustomSectionTemplate> customTemplates = loadCustomTemplates(request.getCustomSectionIds());
        String markdown = generationService.generateWordCard(normalizedWord, customTemplates, null);

        // 保存 word（若不存在）
        if (word == null) {
            word = new Word();
            word.setWord(normalizedWord);
            word.setSourceLanguage("en");
            word.setSearchCount(1);
            wordMapper.insert(word);
            word = wordMapper.selectById(word.getId());
        } else {
            incrementSearchCount(word);
        }

        // 保存 word_card
        WordCard card = new WordCard();
        card.setWordId(word.getId());
        card.setTitle(normalizedWord);
        card.setRawMarkdown(markdown);
        card.setGeneratedByModel(getDefaultModelName());
        wordCardMapper.insert(card);
        card = wordCardMapper.selectById(card.getId());

        // 解析 markdown → sections
        saveSectionsFromMarkdown(card.getId(), markdown, customTemplates);

        return buildWordCardVO(word, card, false);
    }

    @Override
    public WordCardVO getWordCard(Long wordId) {
        Word word = requireWord(wordId);
        WordCard card = findLatestCard(wordId);
        if (card == null) {
            throw new BusinessException("WORD_CARD_NOT_FOUND", "该单词尚无单词卡");
        }
        return buildWordCardVO(word, card, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WordCardVO regenerateWordCard(Long wordId, WordCardRegenerateRequest request) {
        Word word = requireWord(wordId);
        WordCard oldCard = findLatestCard(wordId);

        // 删除旧 sections
        if (oldCard != null) {
            LambdaQueryWrapper<WordCardSection> delWrapper = new LambdaQueryWrapper<>();
            delWrapper.eq(WordCardSection::getWordCardId, oldCard.getId());
            sectionMapper.delete(delWrapper);
            wordCardMapper.deleteById(oldCard.getId());
        }

        // AI 重新生成
        List<CustomSectionTemplate> customTemplates = loadCustomTemplates(
                request != null ? request.getCustomSectionIds() : null);
        String userPrompt = request != null ? request.getUserPrompt() : null;
        String markdown = generationService.generateWordCard(word.getWord(), customTemplates, userPrompt);

        // 保存新 word_card
        WordCard card = new WordCard();
        card.setWordId(word.getId());
        card.setTitle(word.getWord());
        card.setRawMarkdown(markdown);
        card.setGeneratedByModel(getDefaultModelName());
        wordCardMapper.insert(card);
        card = wordCardMapper.selectById(card.getId());

        saveSectionsFromMarkdown(card.getId(), markdown, customTemplates);

        return buildWordCardVO(word, card, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WordCardVO regenerateSection(Long wordId, Long sectionId, SectionRegenerateRequest request) {
        Word word = requireWord(wordId);
        WordCard card = findLatestCard(wordId);
        if (card == null) {
            throw new BusinessException("WORD_CARD_NOT_FOUND", "该单词尚无单词卡");
        }

        WordCardSection section = sectionMapper.selectById(sectionId);
        if (section == null || !section.getWordCardId().equals(card.getId())) {
            throw new BusinessException("SECTION_NOT_FOUND", "模块不存在");
        }

        // AI 重新生成该模块内容
        String userPrompt = request != null ? request.getUserPrompt() : null;
        String newContent = generationService.regenerateSection(
                word.getWord(), section.getSectionTitle(), card.getRawMarkdown(), userPrompt);

        // 更新 section
        section.setSectionContent(newContent.trim());
        sectionMapper.updateById(section);

        // 重新拼接完整 markdown
        rebuildRawMarkdown(card);

        return buildWordCardVO(word, card, false);
    }

    @Override
    public ChineseLookupVO chineseLookup(ChineseLookupRequest request) {
        String chinese = request.getChinese().trim();
        String json = generationService.lookupChineseCandidates(chinese);

        // 清理 AI 返回中可能包含的 markdown 代码块标记
        json = json.trim();
        if (json.startsWith("```")) {
            json = json.replaceFirst("```(?:json)?\\s*", "");
            json = json.replaceFirst("\\s*```$", "");
        }

        List<CandidateWordVO> candidates;
        try {
            candidates = objectMapper.readValue(json, new TypeReference<List<CandidateWordVO>>() {});
        } catch (Exception e) {
            log.error("解析 AI 返回的候选词 JSON 失败: {}", json, e);
            throw new BusinessException("CHINESE_LOOKUP_PARSE_FAILED", "AI 返回结果解析失败，请重试");
        }

        return ChineseLookupVO.builder()
                .chinese(chinese)
                .candidates(candidates)
                .build();
    }

    // ==================== 私有方法 ====================

    private String normalize(String word) {
        return word.trim().toLowerCase(Locale.ROOT);
    }

    private Word findWord(String normalizedWord) {
        LambdaQueryWrapper<Word> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Word::getWord, normalizedWord);
        return wordMapper.selectOne(wrapper);
    }

    private Word requireWord(Long wordId) {
        Word word = wordMapper.selectById(wordId);
        if (word == null) {
            throw new BusinessException("WORD_NOT_FOUND", "单词不存在");
        }
        return word;
    }

    private WordCard findLatestCard(Long wordId) {
        LambdaQueryWrapper<WordCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WordCard::getWordId, wordId)
                .orderByDesc(WordCard::getId)
                .last("LIMIT 1");
        return wordCardMapper.selectOne(wrapper);
    }

    private void incrementSearchCount(Word word) {
        LambdaUpdateWrapper<Word> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Word::getId, word.getId())
                .setSql("search_count = search_count + 1");
        wordMapper.update(null, wrapper);
        word.setSearchCount(word.getSearchCount() + 1);
    }

    private List<CustomSectionTemplate> loadCustomTemplates(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return templateMapper.selectBatchIds(ids);
    }

    /**
     * 解析 AI 生成的 markdown，按 ## 标题分割为 sections 并入库
     */
    private void saveSectionsFromMarkdown(Long wordCardId, String markdown, List<CustomSectionTemplate> customTemplates) {
        List<ParsedSection> parsed = parseMarkdownSections(markdown);
        int order = 0;
        for (ParsedSection ps : parsed) {
            WordCardSection section = new WordCardSection();
            section.setWordCardId(wordCardId);
            section.setSectionKey(generateSectionKey(ps.title));
            section.setSectionTitle(ps.title);
            section.setSectionContent(ps.content.trim());
            section.setSortOrder(order++);
            section.setIsPreset(PRESET_SECTION_TITLES.contains(ps.title) ? 1 : 0);
            sectionMapper.insert(section);
        }
    }

    /**
     * 从 markdown 中解析出 ## 标题和内容
     */
    private List<ParsedSection> parseMarkdownSections(String markdown) {
        List<ParsedSection> sections = new ArrayList<>();
        Matcher matcher = SECTION_PATTERN.matcher(markdown);

        List<int[]> titlePositions = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        while (matcher.find()) {
            titlePositions.add(new int[]{matcher.start(), matcher.end()});
            titles.add(matcher.group(1).trim());
        }

        for (int i = 0; i < titlePositions.size(); i++) {
            int contentStart = titlePositions.get(i)[1];
            int contentEnd = (i + 1 < titlePositions.size()) ? titlePositions.get(i + 1)[0] : markdown.length();
            String content = markdown.substring(contentStart, contentEnd).trim();
            sections.add(new ParsedSection(titles.get(i), content));
        }

        return sections;
    }

    private String generateSectionKey(String title) {
        // 将中文标题转为简单 key：转拼音太复杂，直接使用标题的 hashCode
        return "section_" + Math.abs(title.hashCode());
    }

    /**
     * 重新拼接 word_card 的 raw_markdown（在单模块重新生成后）
     */
    private void rebuildRawMarkdown(WordCard card) {
        LambdaQueryWrapper<WordCardSection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WordCardSection::getWordCardId, card.getId())
                .orderByAsc(WordCardSection::getSortOrder);
        List<WordCardSection> sections = sectionMapper.selectList(wrapper);

        StringBuilder sb = new StringBuilder();
        for (WordCardSection s : sections) {
            sb.append("## ").append(s.getSectionTitle()).append("\n\n");
            sb.append(s.getSectionContent()).append("\n\n");
        }

        card.setRawMarkdown(sb.toString().trim());
        wordCardMapper.updateById(card);
    }

    private WordCardVO buildWordCardVO(Word word, WordCard card, boolean cached) {
        // 获取 sections
        LambdaQueryWrapper<WordCardSection> secWrapper = new LambdaQueryWrapper<>();
        secWrapper.eq(WordCardSection::getWordCardId, card.getId())
                .orderByAsc(WordCardSection::getSortOrder);
        List<WordCardSection> sections = sectionMapper.selectList(secWrapper);

        List<SectionVO> sectionVOs = sections.stream()
                .map(s -> SectionVO.builder()
                        .id(s.getId())
                        .sectionKey(s.getSectionKey())
                        .sectionTitle(s.getSectionTitle())
                        .sectionContent(s.getSectionContent())
                        .sortOrder(s.getSortOrder())
                        .preset(s.getIsPreset() == 1)
                        .build())
                .toList();

        WordCardDetailVO detail = WordCardDetailVO.builder()
                .id(card.getId())
                .title(card.getTitle())
                .rawMarkdown(card.getRawMarkdown())
                .generatedByModel(card.getGeneratedByModel())
                .sections(sectionVOs)
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .build();

        // 重新从数据库读取最新 word（search_count 可能已更新）
        Word latestWord = wordMapper.selectById(word.getId());

        return WordCardVO.builder()
                .wordId(latestWord.getId())
                .word(latestWord.getWord())
                .searchCount(latestWord.getSearchCount())
                .cached(cached)
                .wordCard(detail)
                .build();
    }

    private String getDefaultModelName() {
        try {
            LambdaQueryWrapper<ModelConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ModelConfig::getIsWordGenerationDefault, 1).last("LIMIT 1");
            ModelConfig config = modelConfigMapper.selectOne(wrapper);
            return config != null ? config.getModelName() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    private record ParsedSection(String title, String content) {}
}