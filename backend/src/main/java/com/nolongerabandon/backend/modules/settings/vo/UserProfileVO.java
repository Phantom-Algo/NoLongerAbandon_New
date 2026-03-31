package com.nolongerabandon.backend.modules.settings.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileVO {

    private Long id;

    private String nickname;

    private String profileMarkdown;

    private Boolean allowAiReadProfile;

    private String updatedAt;
}
