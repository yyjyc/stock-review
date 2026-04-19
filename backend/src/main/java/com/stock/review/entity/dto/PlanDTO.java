package com.stock.review.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PlanDTO {
    
    private Long id;
    
    private String sourceType;
    
    private String stockName;
    
    private String stockCode;
    
    private String planType;
    
    private Integer planShares;
    
    private BigDecimal planAmount;
    
    private String planReason;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planDate;
    
    private String executeTime;
    
    private String planStatus;
    
    private BigDecimal targetPrice;
    
    private BigDecimal stopLossPrice;
    
    private Integer executed;
    
    private Boolean canUpdate;
    
    private Integer actualShares;
    
    private BigDecimal actualAmount;
    
    private BigDecimal actualPrice;
    
    private BigDecimal profitLossRatio;
}
