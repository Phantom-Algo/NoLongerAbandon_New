package com.nolongerabandon.backend.modules.settings.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("model_config")
public class ModelConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String providerName;

    private String displayName;

    private String baseUrl;

    private String modelName;

    private String apiKeyEncrypted;

    private Integer enabled;

    private Integer isWordGenerationDefault;

    private String completionsPath;

    private String createdAt;

    private String updatedAt;
}
