package com.stock.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stock.review.entity.TradeCalendar;

import java.time.LocalDate;
import java.util.List;

public interface TradeCalendarService extends IService<TradeCalendar> {
    
    boolean isTradeDay(LocalDate date);
    
    boolean canUpdatePlan(LocalDate planDate);
    
    int countTradeDaysBetween(LocalDate startDate, LocalDate endDate);
    
    LocalDate getNextTradeDay(LocalDate date);
    
    LocalDate getPreviousTradeDay(LocalDate date);
    
    LocalDate getCurrentReviewTradeDate();
    
    LocalDate getNextTradeDayForPlan();
    
    void batchInsert(List<TradeCalendar> calendars);
}
