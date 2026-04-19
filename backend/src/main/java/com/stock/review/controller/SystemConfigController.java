package com.stock.review.controller;

import com.stock.review.common.Result;
import com.stock.review.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class SystemConfigController {
    
    private final SystemConfigService systemConfigService;
    
    @GetMapping("/outflow-threshold")
    public Result<BigDecimal> getOutflowThreshold() {
        return Result.success(systemConfigService.getOutflowThreshold());
    }
    
    @GetMapping("/inflow-threshold")
    public Result<BigDecimal> getInflowThreshold() {
        return Result.success(systemConfigService.getInflowThreshold());
    }
    
    @PostMapping("/outflow-threshold")
    public Result<Void> setOutflowThreshold(@RequestParam BigDecimal threshold) {
        systemConfigService.setOutflowThreshold(threshold);
        return Result.success();
    }
    
    @PostMapping("/inflow-threshold")
    public Result<Void> setInflowThreshold(@RequestParam BigDecimal threshold) {
        systemConfigService.setInflowThreshold(threshold);
        return Result.success();
    }
}
