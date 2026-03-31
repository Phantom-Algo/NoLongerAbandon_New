package com.nolongerabandon.backend.modules.wordcard.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WordCardDetailVO {

    private Long id;

    private String title;

    private String rawMarkdown;

    private String generatedByModel;

    private List<SectionVO> sections;

    private String createdAt;

    private String updatedAt;
}
