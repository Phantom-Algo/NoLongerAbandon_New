package com.nolongerabandon.backend.modules.wordcard.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChineseLookupRequest {

    @NotBlank(message = "中文关键词不能为空")
    private String chinese;
}
