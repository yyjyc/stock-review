package com.stock.review.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("trade_plan")
public class TradePlan implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String sourceType;
    
    private Long sourceId;
    
    private String stockName;
    
    private String stockCode;
    
    private String planType;
    
    private Integer planShares;
    
    private BigDecimal planAmount;
    
    private BigDecimal planPrice;
    
    private String planReason;
    
    private LocalDate planDate;
    
    private String executeTime;
    
    private BigDecimal targetPrice;
    
    private BigDecimal stopLossPrice;
    
    private String planStatus;
    
    private Integer actualShares;
    
    private BigDecimal actualAmount;
    
    private BigDecimal actualPrice;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
