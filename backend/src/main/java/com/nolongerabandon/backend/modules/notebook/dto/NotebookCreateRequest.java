package com.nolongerabandon.backend.modules.notebook.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NotebookCreateRequest {

    @NotBlank(message = "单词本名称不能为空")
    private String name;

    private String description;
}
