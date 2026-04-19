package com.stock.review.entity.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ActiveMarketValueDTO {
    private Long id;
    
    private LocalDate recordDate;
    
    private BigDecimal marketValue;
    
    private String marketStatus;
    
    private String operationTip;
}
