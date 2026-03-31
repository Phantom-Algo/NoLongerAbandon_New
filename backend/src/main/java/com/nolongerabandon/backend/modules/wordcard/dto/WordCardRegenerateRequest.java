package com.nolongerabandon.backend.modules.wordcard.dto;

import java.util.List;
import lombok.Data;

@Data
public class WordCardRegenerateRequest {

    /**
     * 用户选中的自定义模块模板 ID 列表（可选）
     */
    private List<Long> customSectionIds;

    /**
     * 用户额外的提示词（可选）
     */
    private String userPrompt;
}
