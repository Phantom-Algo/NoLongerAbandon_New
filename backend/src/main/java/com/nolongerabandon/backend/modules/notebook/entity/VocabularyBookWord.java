package com.nolongerabandon.backend.modules.notebook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("vocabulary_book_word")
public class VocabularyBookWord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long vocabularyBookId;

    private Long wordId;

    private String createdAt;
}
