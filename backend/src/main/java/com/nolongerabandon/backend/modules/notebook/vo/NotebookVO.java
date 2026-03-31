package com.nolongerabandon.backend.modules.notebook.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotebookVO {

    private Long id;

    private String name;

    private String description;

    private Integer wordCount;

    private String createdAt;

    private String updatedAt;
}
