package com.nolongerabandon.backend.modules.wordcard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("word_card_section")
public class WordCardSection {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long wordCardId;

    private String sectionKey;

    private String sectionTitle;

    private String sectionContent;

    private Integer sortOrder;

    private Integer isPreset;

    private String createdAt;

    private String updatedAt;
}
