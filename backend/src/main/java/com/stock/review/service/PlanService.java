package com.stock.review.service;

import com.stock.review.entity.dto.PlanDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PlanService {
    
    List<PlanDTO> getTodayPlans(String planStatus);
    
    List<PlanDTO> getPlansByDateRange(LocalDate startDate, LocalDate endDate, String planStatus);
    
    void executePlan(Long id, String sourceType, Integer shares, BigDecimal price);
    
    void deletePlan(Long id, String sourceType);
    
    void updateOverduePlans();
}
