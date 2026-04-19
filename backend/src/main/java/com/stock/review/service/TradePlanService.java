package com.stock.review.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stock.review.entity.TradePlan;
import com.stock.review.entity.dto.PlanDTO;

import java.time.LocalDate;
import java.util.List;

public interface TradePlanService extends IService<TradePlan> {
    
    List<PlanDTO> getPlansByDateRange(LocalDate startDate, LocalDate endDate, String planStatus);
    
    List<PlanDTO> getTodayPlans(String planStatus);
    
    void executePlan(Long id, Integer shares, java.math.BigDecimal price);
    
    void markAsNotExecuted(Long id);
    
    void updateExpiredPlans();
    
    void syncFromPosition(Long positionId, String stockName, String stockCode, String planType, Integer planShares, java.math.BigDecimal planAmount, String planReason, LocalDate planDate, String executeTime);
    
    void syncFromSelection(Long selectionId, String stockName, String stockCode, Integer planShares, java.math.BigDecimal planAmount, String planReason, LocalDate planDate, String executeTime, java.math.BigDecimal targetPrice, java.math.BigDecimal stopLossPrice);
    
    void deleteBySource(String sourceType, Long sourceId);
    
    Page<PlanDTO> pageList(Integer pageNum, Integer pageSize, String startDate, String endDate, String planStatus, String stockName);
}
