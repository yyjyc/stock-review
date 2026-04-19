package com.stock.review.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OperationStatisticsDTO {
    private Long totalCount;
    private Long followRuleCount;
    private Long notFollowRuleCount;
    private BigDecimal followRulePercent;
    private BigDecimal notFollowRulePercent;
}
