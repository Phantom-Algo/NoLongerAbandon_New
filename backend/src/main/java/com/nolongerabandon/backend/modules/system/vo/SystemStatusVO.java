package com.nolongerabandon.backend.modules.system.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemStatusVO {

    private String applicationName;

    private List<String> activeProfiles;

    private String storagePath;
}