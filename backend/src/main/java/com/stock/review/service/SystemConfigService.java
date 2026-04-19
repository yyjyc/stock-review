package com.stock.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stock.review.entity.SystemConfig;

import java.math.BigDecimal;

public interface SystemConfigService extends IService<SystemConfig> {
    
    String getValue(String key);
    
    void setValue(String key, String value, String desc);
    
    BigDecimal getOutflowThreshold();
    
    BigDecimal getInflowThreshold();
    
    void setOutflowThreshold(BigDecimal threshold);
    
    void setInflowThreshold(BigDecimal threshold);
}
