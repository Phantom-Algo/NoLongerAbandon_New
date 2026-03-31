package com.nolongerabandon.backend.modules.wordcard.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WordCardVO {

    private Long wordId;

    private String word;

    private Integer searchCount;

    private boolean cached;

    private WordCardDetailVO wordCard;
}
