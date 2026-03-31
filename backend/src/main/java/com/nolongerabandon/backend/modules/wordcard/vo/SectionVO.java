package com.nolongerabandon.backend.modules.wordcard.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SectionVO {

    private Long id;

    private String sectionKey;

    private String sectionTitle;

    private String sectionContent;

    private Integer sortOrder;

    private boolean preset;
}
