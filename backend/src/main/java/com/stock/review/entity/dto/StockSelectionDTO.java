package com.stock.review.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StockSelectionDTO {
    private Long id;
    
    @NotBlank(message = "股票名称不能为空")
    private String stockName;
    
    @NotBlank(message = "股票代码不能为空")
    private String stockCode;
    
    @NotNull(message = "目标价格不能为空")
    private BigDecimal targetPrice;
    
    @NotNull(message = "止损价格不能为空")
    private BigDecimal stopLossPrice;
    
    @NotNull(message = "计划金额不能为空")
    private BigDecimal planAmount;
    
    @NotBlank(message = "选股理由不能为空")
    private String selectionReason;
    
    @NotNull(message = "选股日期不能为空")
    private LocalDate selectionDate;
    
    private Integer planShares;
    
    private LocalDate planDate;
    
    private String executeTime;
}
