package com.stock.review.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.review.common.Result;
import com.stock.review.entity.dto.PlanDTO;
import com.stock.review.service.TradePlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/plan")
@RequiredArgsConstructor
public class PlanController {
    
    private final TradePlanService tradePlanService;
    
    @GetMapping("/today")
    public Result<List<PlanDTO>> getTodayPlans(
            @RequestParam(required = false) String planStatus) {
        return Result.success(tradePlanService.getTodayPlans(planStatus));
    }
    
    @GetMapping("/range")
    public Result<List<PlanDTO>> getPlansByDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String planStatus) {
        return Result.success(tradePlanService.getPlansByDateRange(startDate, endDate, planStatus));
    }
    
    @PostMapping("/execute")
    public Result<Void> executePlan(@RequestParam Long id,
                                    @RequestParam Integer shares,
                                    @RequestParam BigDecimal price) {
        tradePlanService.executePlan(id, shares, price);
        return Result.success();
    }
    
    @PostMapping("/not-executed")
    public Result<Void> markAsNotExecuted(@RequestParam Long id) {
        tradePlanService.markAsNotExecuted(id);
        return Result.success();
    }
    
    @GetMapping("/page")
    public Result<Page<PlanDTO>> pageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String planStatus,
            @RequestParam(required = false) String stockName) {
        return Result.success(tradePlanService.pageList(pageNum, pageSize, startDate, endDate, planStatus, stockName));
    }
}
