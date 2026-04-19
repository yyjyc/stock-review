package com.stock.review.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stock.review.entity.ActiveMarketValue;
import com.stock.review.entity.dto.ActiveMarketValueDTO;

import java.math.BigDecimal;
import java.util.List;

public interface ActiveMarketValueService extends IService<ActiveMarketValue> {
    
    ActiveMarketValue saveOrUpdate(ActiveMarketValueDTO dto);
    
    Page<ActiveMarketValue> pageList(Integer pageNum, Integer pageSize);
    
    ActiveMarketValue getLatest();
    
    List<ActiveMarketValue> getRecentList(Integer days);
    
    String judgeMarketStatus(BigDecimal changePercent, BigDecimal prevChangePercent, String currentStatus);
    
    String getOperationTip(String marketStatus);
    
    void recalculateAllStatus();
}
