package com.stock.review.controller;

import com.stock.review.common.Result;
import com.stock.review.service.TradeCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/trade-calendar")
@RequiredArgsConstructor
public class TradeCalendarController {
    
    private final TradeCalendarService tradeCalendarService;
    
    @GetMapping("/current-review-date")
    public Result<Map<String, Object>> getCurrentReviewDate() {
        LocalDate reviewDate = tradeCalendarService.getCurrentReviewTradeDate();
        Map<String, Object> data = new HashMap<>();
        data.put("reviewDate", reviewDate != null ? reviewDate.toString() : null);
        return Result.success(data);
    }
    
    @GetMapping("/next-trade-day")
    public Result<Map<String, Object>> getNextTradeDay() {
        LocalDate nextTradeDay = tradeCalendarService.getNextTradeDayForPlan();
        Map<String, Object> data = new HashMap<>();
        data.put("nextTradeDay", nextTradeDay != null ? nextTradeDay.toString() : null);
        return Result.success(data);
    }
    
    @GetMapping("/trade-date-info")
    public Result<Map<String, Object>> getTradeDateInfo() {
        LocalDate reviewDate = tradeCalendarService.getCurrentReviewTradeDate();
        LocalDate nextTradeDay = tradeCalendarService.getNextTradeDayForPlan();
        LocalDate today = LocalDate.now();
        
        Map<String, Object> data = new HashMap<>();
        data.put("today", today.toString());
        data.put("reviewDate", reviewDate != null ? reviewDate.toString() : null);
        data.put("nextTradeDay", nextTradeDay != null ? nextTradeDay.toString() : null);
        data.put("isTradeDay", tradeCalendarService.isTradeDay(today));
        return Result.success(data);
    }
}
