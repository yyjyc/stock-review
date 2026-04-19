package com.stock.review.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PositionSummaryDTO {
    private Integer totalCount;
    private BigDecimal totalAmount;
    private BigDecimal totalProfitLoss;
    private BigDecimal totalProfitLossPercent;
}
