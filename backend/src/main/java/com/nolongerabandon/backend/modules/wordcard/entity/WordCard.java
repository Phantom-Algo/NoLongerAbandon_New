package com.nolongerabandon.backend.modules.wordcard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("word_card")
public class WordCard {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long wordId;

    private String title;

    private String rawMarkdown;

    private String generatedByModel;

    private String createdAt;

    private String updatedAt;
}
