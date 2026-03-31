package com.nolongerabandon.backend.modules.settings.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ModelConfigSaveRequest {

    @NotBlank(message = "providerName 不能为空")
    private String providerName;

    @NotBlank(message = "displayName 不能为空")
    private String displayName;

    private String baseUrl;

    @NotBlank(message = "modelName 不能为空")
    private String modelName;

    private String apiKey;

    private Boolean enabled;

    private Boolean wordGenerationDefault;

    private String completionsPath;
}
