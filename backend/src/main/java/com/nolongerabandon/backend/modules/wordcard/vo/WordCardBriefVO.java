package com.nolongerabandon.backend.modules.wordcard.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WordCardBriefVO {

    private String word;

    private boolean cached;

    private Integer searchCount;

    private String nextAction;
}