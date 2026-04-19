package com.stock.review.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("active_market_value")
public class ActiveMarketValue implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private LocalDate recordDate;
    
    private BigDecimal marketValue;
    
    private BigDecimal changePercent;
    
    private String marketStatus;
    
    private String operationTip;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
