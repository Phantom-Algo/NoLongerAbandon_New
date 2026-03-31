package com.nolongerabandon.backend.modules.wordcard.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WordLookupRequest {

    @NotBlank(message = "单词不能为空")
    private String word;
}