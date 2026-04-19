package com.stock.review.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("position")
public class Position implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String stockName;
    
    private String stockCode;
    
    private BigDecimal holdAmount;
    
    private Integer holdShares;
    
    private BigDecimal costPrice;
    
    private BigDecimal currentPrice;
    
    private BigDecimal targetPrice;
    
    private BigDecimal stopLossPrice;
    
    private BigDecimal profitLoss;
    
    private BigDecimal profitLossPercent;
    
    private BigDecimal profitLossRatio;
    
    private String status;
    
    private BigDecimal clearPrice;
    
    private BigDecimal clearProfitLoss;
    
    private LocalDate clearDate;
    
    private String planType;
    
    private Integer planShares;
    
    private BigDecimal planAmount;
    
    private String planReason;
    
    private LocalDate planDate;
    
    private String executeTime;
    
    private String planStatus;
    
    private String alertStatus;
    
    private LocalDateTime planCreateTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
