package com.nolongerabandon.backend.modules.notebook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("vocabulary_book")
public class VocabularyBook {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private String createdAt;

    private String updatedAt;
}
