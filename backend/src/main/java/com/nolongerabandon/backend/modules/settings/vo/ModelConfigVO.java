package com.nolongerabandon.backend.modules.settings.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModelConfigVO {

    private Long id;

    private String providerName;

    private String displayName;

    private String baseUrl;

    private String modelName;

    private String apiKeyMasked;

    private Boolean enabled;

    private Boolean wordGenerationDefault;

    private String completionsPath;

    private String createdAt;

    private String updatedAt;
}
