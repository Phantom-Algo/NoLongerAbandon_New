package com.nolongerabandon.backend.modules.wordcard.dto;

import lombok.Data;

@Data
public class SectionRegenerateRequest {

    /**
     * 用户额外的提示词（可选）
     */
    private String userPrompt;
}
