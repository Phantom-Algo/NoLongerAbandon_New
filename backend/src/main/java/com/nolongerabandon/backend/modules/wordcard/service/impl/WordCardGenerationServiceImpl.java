package com.nolongerabandon.backend.modules.wordcard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nolongerabandon.backend.common.exception.BusinessException;
import com.nolongerabandon.backend.common.util.CryptoUtil;
import com.nolongerabandon.backend.modules.settings.entity.ModelConfig;
import com.nolongerabandon.backend.modules.settings.mapper.ModelConfigMapper;
import com.nolongerabandon.backend.modules.wordcard.entity.CustomSectionTemplate;
import com.nolongerabandon.backend.modules.wordcard.service.WordCardGenerationService;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WordCardGenerationServiceImpl implements WordCardGenerationService {

    /** 预设模块 */
    private static final List<String> PRESET_SECTIONS = List.of(
            "基本释义", "音标", "例句", "短语", "记背方法"
    );

    private final ModelConfigMapper modelConfigMapper;

    public WordCardGenerationServiceImpl(ModelConfigMapper modelConfigMapper) {
        this.modelConfigMapper = modelConfigMapper;
    }

    @Override
    public String generateWordCard(String word, List<CustomSectionTemplate> customTemplates, String userPrompt) {
        // 构建模块列表
        List<String> sectionDescriptions = new ArrayList<>();
        int order = 1;
        for (String preset : PRESET_SECTIONS) {
            sectionDescriptions.add(order + ". " + preset);
            order++;
        }
        if (customTemplates != null) {
            for (CustomSectionTemplate tpl : customTemplates) {
                sectionDescriptions.add(order + ". " + tpl.getTitle() + "（要求：" + tpl.getPrompt() + "）");
                order++;
            }
        }

        String sectionList = String.join("\n", sectionDescriptions);

        String systemPrompt = """
                你是一个专业的英语学习助手。用户会给你一个英文单词，你需要按照指定的模块生成一张完整的单词卡。
                
                输出要求：
                1. 使用 Markdown 格式
                2. 每个模块使用 ## 作为标题（二级标题）
                3. 模块标题必须与要求的模块名称完全一致
                4. 内容必须准确、实用、适合英语学习者
                5. 例句模块至少提供 3 个例句，每个例句附带中文翻译
                6. 短语模块至少提供 3 个常用短语/搭配，附带中文释义
                7. 记背方法要有创意且实用
                8. 不要在最开始输出一级标题（#），直接从第一个 ## 模块开始
                """;

        String userContent = "请为单词「" + word + "」生成单词卡，包含以下模块：\n" + sectionList;
        if (userPrompt != null && !userPrompt.isBlank()) {
            userContent += "\n\n额外要求：" + userPrompt;
        }

        return callAi(systemPrompt, userContent);
    }

    @Override
    public String regenerateSection(String word, String sectionTitle, String currentMarkdown, String userPrompt) {
        String systemPrompt = """
                你是一个专业的英语学习助手。用户需要你重新生成单词卡中某一个特定模块的内容。
                
                输出要求：
                1. 只输出该模块的内容，不要输出模块标题（## 标题由系统自动处理）
                2. 内容必须准确、实用
                3. 不要输出其他模块的内容
                """;

        String userContent = "单词：" + word + "\n"
                + "需要重新生成的模块：" + sectionTitle + "\n"
                + "当前单词卡完整内容（作为参考上下文）：\n" + currentMarkdown;
        if (userPrompt != null && !userPrompt.isBlank()) {
            userContent += "\n\n额外要求：" + userPrompt;
        }

        return callAi(systemPrompt, userContent);
    }

    @Override
    public String lookupChineseCandidates(String chinese) {
        String systemPrompt = """
                你是一个专业的英语学习助手。用户会给你一个中文词汇或短语，你需要找出 3~5 个最合适的对应英文单词。
                
                输出要求：
                1. 严格按照以下 JSON 格式输出，不要输出其他内容
                2. 每个候选词包含 word（英文单词）和 definition（中文释义）
                3. 按照匹配度从高到低排序
                
                输出格式：
                [{"word":"example","definition":"中文释义"},{"word":"sample","definition":"中文释义"}]
                """;

        String userContent = "请为中文「" + chinese + "」找出合适的英文单词。";

        return callAi(systemPrompt, userContent);
    }

    /**
     * 获取默认模型配置并调用 AI
     */
    private String callAi(String systemPrompt, String userContent) {
        ModelConfig defaultModel = getDefaultModelConfig();

        String apiKey;
        try {
            apiKey = CryptoUtil.decrypt(defaultModel.getApiKeyEncrypted());
        } catch (Exception e) {
            throw new BusinessException("MODEL_KEY_DECRYPT_FAILED", "默认模型的 API Key 解密失败，请在设置中重新配置");
        }

        String baseUrl = defaultModel.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.openai.com";
        }

        try {
            OpenAiApi openAiApi = new OpenAiApi(baseUrl, apiKey);
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .model(defaultModel.getModelName())
                    .temperature(0.7)
                    .build();
            OpenAiChatModel chatModel = new OpenAiChatModel(openAiApi, options);

            SystemMessage sysMsg = new SystemMessage(systemPrompt);
            UserMessage userMsg = new UserMessage(userContent);

            ChatResponse response = chatModel.call(new Prompt(List.of(sysMsg, userMsg)));
            return response.getResult().getOutput().getText();
        } catch (Exception e) {
            log.error("AI 调用失败", e);
            throw new BusinessException("AI_CALL_FAILED", "AI 生成失败：" + e.getMessage());
        }
    }

    /**
     * 获取 is_word_generation_default=1 的模型配置
     */
    private ModelConfig getDefaultModelConfig() {
        LambdaQueryWrapper<ModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelConfig::getIsWordGenerationDefault, 1)
                .eq(ModelConfig::getEnabled, 1)
                .last("LIMIT 1");
        ModelConfig config = modelConfigMapper.selectOne(wrapper);
        if (config == null) {
            throw new BusinessException("NO_DEFAULT_MODEL", "尚未配置默认单词生成模型，请在设置中配置");
        }
        if (config.getApiKeyEncrypted() == null || config.getApiKeyEncrypted().isBlank()) {
            throw new BusinessException("MODEL_KEY_MISSING", "默认模型的 API Key 未配置，请在设置中配置");
        }
        return config;
    }
}
