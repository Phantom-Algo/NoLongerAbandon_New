package com.nolongerabandon.backend.modules.settings.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_profile")
public class UserProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String nickname;

    private String profileMarkdown;

    private Integer allowAiReadProfile;

    private String updatedAt;
}
