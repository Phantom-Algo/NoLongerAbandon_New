package com.nolongerabandon.backend.modules.notebook.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotebookAddWordRequest {

    @NotNull(message = "单词ID不能为空")
    private Long wordId;
}
