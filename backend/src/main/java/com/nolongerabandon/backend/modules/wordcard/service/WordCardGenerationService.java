package com.nolongerabandon.backend.modules.wordcard.service;

import com.nolongerabandon.backend.modules.wordcard.entity.CustomSectionTemplate;
import java.util.List;

/**
 * AI 生成单词卡服务
 */
public interface WordCardGenerationService {

    /**
     * 根据英文单词 + 预设模块 + 自定义模块调用 AI 生成单词卡 markdown
     *
     * @param word              英文单词
     * @param customTemplates   用户选中的自定义模块模板列表
     * @param userPrompt        用户额外的提示词（可选）
     * @return AI 生成的完整 markdown 文本
     */
    String generateWordCard(String word, List<CustomSectionTemplate> customTemplates, String userPrompt);

    /**
     * 根据已有单词卡上下文，重新生成某一个特定模块
     *
     * @param word              英文单词
     * @param sectionTitle      需要重新生成的模块标题
     * @param currentMarkdown   当前单词卡的完整 markdown（作为上下文）
     * @param userPrompt        用户额外的提示词（可选）
     * @return 该模块重新生成后的内容（不含标题行）
     */
    String regenerateSection(String word, String sectionTitle, String currentMarkdown, String userPrompt);

    /**
     * 根据中文关键词，查找合适的英文候选词
     *
     * @param chinese 中文关键词
     * @return AI 返回的 JSON 格式文本，包含候选词及释义
     */
    String lookupChineseCandidates(String chinese);
}
