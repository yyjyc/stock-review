package com.stock.review.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StockOperationDTO {
    private Long id;
    
    @NotBlank(message = "股票名称不能为空")
    private String stockName;
    
    @NotBlank(message = "股票代码不能为空")
    private String stockCode;
    
    @NotBlank(message = "操作类型不能为空")
    private String operationType;
    
    @NotNull(message = "操作金额不能为空")
    private BigDecimal operationAmount;
    
    @NotNull(message = "操作价格不能为空")
    private BigDecimal operationPrice;
    
    @NotBlank(message = "操作原因不能为空")
    private String operationReason;
    
    @NotNull(message = "是否符合纪律不能为空")
    private Integer isFollowRule;
    
    @NotNull(message = "操作日期不能为空")
    private LocalDate operationDate;
    
    private Long positionId;
}
