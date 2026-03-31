package com.nolongerabandon.backend.modules.settings.dto;

import lombok.Data;

@Data
public class UserProfileUpdateRequest {

    private String nickname;

    private String profileMarkdown;

    private Boolean allowAiReadProfile;
}
