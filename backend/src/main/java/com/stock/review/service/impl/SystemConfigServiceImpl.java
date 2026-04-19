package com.stock.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.review.entity.SystemConfig;
import com.stock.review.mapper.SystemConfigMapper;
import com.stock.review.service.SystemConfigService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {
    
    private static final String OUTFLOW_THRESHOLD_KEY = "outflow_threshold";
    private static final String INFLOW_THRESHOLD_KEY = "inflow_threshold";
    
    @Override
    public String getValue(String key) {
        SystemConfig config = getOne(new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, key));
        return config != null ? config.getConfigValue() : null;
    }
    
    @Override
    public void setValue(String key, String value, String desc) {
        SystemConfig config = getOne(new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, key));
        
        if (config == null) {
            config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigDesc(desc);
        }
        
        config.setConfigValue(value);
        saveOrUpdate(config);
    }
    
    @Override
    public BigDecimal getOutflowThreshold() {
        String value = getValue(OUTFLOW_THRESHOLD_KEY);
        if (value != null) {
            return new BigDecimal(value);
        }
        return new BigDecimal("-2.3");
    }
    
    @Override
    public BigDecimal getInflowThreshold() {
        String value = getValue(INFLOW_THRESHOLD_KEY);
        if (value != null) {
            return new BigDecimal(value);
        }
        return new BigDecimal("4");
    }
    
    @Override
    public void setOutflowThreshold(BigDecimal threshold) {
        setValue(OUTFLOW_THRESHOLD_KEY, threshold.toString(), "资金流出阈值(%)");
    }
    
    @Override
    public void setInflowThreshold(BigDecimal threshold) {
        setValue(INFLOW_THRESHOLD_KEY, threshold.toString(), "资金流入阈值(%)");
    }
}
