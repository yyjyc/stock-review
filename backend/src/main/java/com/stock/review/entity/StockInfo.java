package com.stock.review.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("stock_info")
public class StockInfo implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String stockCode;
    
    private String stockName;
    
    private String pinyin;
    
    private String firstLetter;
    
    private String market;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
