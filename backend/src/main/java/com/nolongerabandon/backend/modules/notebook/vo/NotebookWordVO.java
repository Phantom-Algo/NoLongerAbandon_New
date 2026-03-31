package com.nolongerabandon.backend.modules.notebook.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotebookWordVO {

    private Long id;

    private Long wordId;

    private String word;

    private String sourceLanguage;

    private Integer searchCount;

    private String addedAt;
}
