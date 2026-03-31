package com.nolongerabandon.backend.modules.wordcard.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Data;

@Data
public class WordSearchRequest {

    @NotBlank(message = "单词不能为空")
    private String word;

    /**
     * 用户选中的自定义模块模板 ID 列表（可选）
     */
    private List<Long> customSectionIds;
}
