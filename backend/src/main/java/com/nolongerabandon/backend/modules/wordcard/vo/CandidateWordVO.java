package com.nolongerabandon.backend.modules.wordcard.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateWordVO {

    private String word;

    private String definition;
}
