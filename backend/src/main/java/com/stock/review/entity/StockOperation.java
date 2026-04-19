package com.stock.review.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("stock_operation")
public class StockOperation implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String stockName;
    
    private String stockCode;
    
    private String operationType;
    
    private BigDecimal operationAmount;
    
    private BigDecimal operationPrice;
    
    private String operationReason;
    
    private Integer isFollowRule;
    
    private LocalDate operationDate;
    
    private Long positionId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
