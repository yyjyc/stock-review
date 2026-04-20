package com.stock.review.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("adjust_reason")
public class AdjustReason {
    
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    
    private String reasonName;
    
    private String reasonContent;
    
    private String reasonType;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
