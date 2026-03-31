package com.nolongerabandon.backend.modules.wordcard.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomSectionTemplateVO {

    private Long id;

    private String title;

    private String prompt;

    private String createdAt;

    private String updatedAt;
}
