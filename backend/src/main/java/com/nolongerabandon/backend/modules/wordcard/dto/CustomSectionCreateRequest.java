package com.nolongerabandon.backend.modules.wordcard.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomSectionCreateRequest {

    @NotBlank(message = "模块主题不能为空")
    private String title;

    @NotBlank(message = "模块内容不能为空")
    private String prompt;
}
