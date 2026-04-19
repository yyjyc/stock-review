package com.stock.review.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SystemConfigDTO {
    private Long id;
    private String configKey;
    private String configValue;
    private String configDesc;
}
