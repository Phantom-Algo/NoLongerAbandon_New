package com.nolongerabandon.backend.modules.wordcard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("custom_section_template")
public class CustomSectionTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String prompt;

    private String createdAt;

    private String updatedAt;
}
