package com.stock.review.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("stock_selection")
public class StockSelection implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    
    private String stockName;
    
    private String stockCode;
    
    private BigDecimal targetPrice;
    
    private BigDecimal stopLossPrice;
    
    private BigDecimal planAmount;
    
    private String selectionReason;
    
    private BigDecimal currentPrice;
    
    private BigDecimal profitLossRatio;
    
    private LocalDate selectionDate;
    
    private Integer operated;
    
    private Integer planShares;
    
    private BigDecimal planPrice;
    
    private LocalDate planDate;
    
    private String executeTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
