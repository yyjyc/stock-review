package com.stock.review.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PositionDTO {
    private Long id;
    
    private String stockName;
    
    private String stockCode;
    
    private BigDecimal holdAmount;
    
    private Integer holdShares;
    
    private BigDecimal costPrice;
    
    private BigDecimal targetPrice;
    
    private BigDecimal stopLossPrice;
    
    private String status;
}
