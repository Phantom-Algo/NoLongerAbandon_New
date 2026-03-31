package com.nolongerabandon.backend.modules.wordcard.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChineseLookupVO {

    private String chinese;

    private List<CandidateWordVO> candidates;
}
