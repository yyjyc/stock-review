package com.stock.review.controller;

import com.stock.review.common.Result;
import com.stock.review.dto.PreloadStatus;
import com.stock.review.entity.StockInfo;
import com.stock.review.service.StockInfoService;
import com.stock.review.utils.StockApiUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {
    
    private final StockInfoService stockInfoService;
    private final StockApiUtil stockApiUtil;
    
    @GetMapping("/search")
    public Result<List<Map<String, Object>>> search(@RequestParam String keyword) {
        List<Map<String, Object>> result = stockApiUtil.searchStock(keyword);
        return Result.success(result);
    }
    
    @GetMapping("/search-local")
    public Result<List<StockInfo>> searchLocal(@RequestParam String keyword) {
        return Result.success(stockInfoService.search(keyword));
    }
    
    @GetMapping("/price/{stockCode}")
    public Result<BigDecimal> getPrice(@PathVariable String stockCode) {
        return Result.success(stockApiUtil.getCurrentPrice(stockCode));
    }
    
    @PostMapping("/batch-price")
    public Result<Map<String, BigDecimal>> batchGetPrice(@RequestBody List<String> stockCodes) {
        return Result.success(stockApiUtil.batchGetCurrentPrice(stockCodes));
    }
    
    @PostMapping("/preload")
    public Result<Void> preloadStockInfo() {
        stockInfoService.preloadStockInfoAsync();
        return Result.success();
    }
    
    @GetMapping("/preload/status")
    public Result<PreloadStatus> getPreloadStatus() {
        return Result.success(stockInfoService.getPreloadStatus());
    }
}
