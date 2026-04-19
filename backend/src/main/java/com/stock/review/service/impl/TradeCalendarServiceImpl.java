package com.stock.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.review.entity.TradeCalendar;
import com.stock.review.mapper.TradeCalendarMapper;
import com.stock.review.service.TradeCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeCalendarServiceImpl extends ServiceImpl<TradeCalendarMapper, TradeCalendar> implements TradeCalendarService {
    
    private static final LocalTime MARKET_OPEN = LocalTime.of(9, 30);
    private static final LocalTime MARKET_CLOSE = LocalTime.of(15, 0);
    
    @Override
    public boolean isTradeDay(LocalDate date) {
        if (date == null) return false;
        
        TradeCalendar calendar = getOne(new LambdaQueryWrapper<TradeCalendar>()
                .eq(TradeCalendar::getTradeDate, date));
        
        if (calendar == null) {
            int dayOfWeek = date.getDayOfWeek().getValue();
            return dayOfWeek >= 1 && dayOfWeek <= 5;
        }
        
        return calendar.getIsTradeDay() != null && calendar.getIsTradeDay() == 1;
    }
    
    @Override
    public boolean canUpdatePlan(LocalDate planDate) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        
        if (today.isBefore(planDate)) {
            return true;
        }
        
        if (today.isEqual(planDate)) {
            return true;
        }
        
        LocalDate nextTradeDay = getNextTradeDay(planDate);
        if (nextTradeDay == null) {
            return true;
        }
        
        if (nextTradeDay.isAfter(today)) {
            return true;
        }
        
        if (nextTradeDay.isEqual(today) && currentTime.isBefore(MARKET_OPEN)) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public int countTradeDaysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || !startDate.isBefore(endDate)) {
            return 0;
        }
        
        LambdaQueryWrapper<TradeCalendar> wrapper = new LambdaQueryWrapper<TradeCalendar>()
                .ge(TradeCalendar::getTradeDate, startDate)
                .lt(TradeCalendar::getTradeDate, endDate)
                .eq(TradeCalendar::getIsTradeDay, 1);
        
        Long count = baseMapper.selectCount(wrapper);
        
        if (count == null || count == 0) {
            int days = 0;
            LocalDate current = startDate;
            while (current.isBefore(endDate)) {
                int dayOfWeek = current.getDayOfWeek().getValue();
                if (dayOfWeek >= 1 && dayOfWeek <= 5) {
                    days++;
                }
                current = current.plusDays(1);
            }
            return days;
        }
        
        return count.intValue();
    }
    
    @Override
    public LocalDate getNextTradeDay(LocalDate date) {
        if (date == null) return null;
        
        LocalDate next = date.plusDays(1);
        int maxDays = 30;
        int count = 0;
        
        while (count < maxDays) {
            if (isTradeDay(next)) {
                return next;
            }
            next = next.plusDays(1);
            count++;
        }
        
        return null;
    }
    
    @Override
    public LocalDate getPreviousTradeDay(LocalDate date) {
        if (date == null) return null;
        
        LocalDate prev = date.minusDays(1);
        int maxDays = 30;
        int count = 0;
        
        while (count < maxDays) {
            if (isTradeDay(prev)) {
                return prev;
            }
            prev = prev.minusDays(1);
            count++;
        }
        
        return null;
    }
    
    @Override
    public LocalDate getCurrentReviewTradeDate() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        
        if (currentTime.isAfter(MARKET_CLOSE) || currentTime.equals(MARKET_CLOSE)) {
            if (isTradeDay(today)) {
                return today;
            } else {
                return getPreviousTradeDay(today);
            }
        } else if (currentTime.isBefore(MARKET_OPEN)) {
            return getPreviousTradeDay(today);
        } else {
            if (isTradeDay(today)) {
                return today;
            } else {
                return getPreviousTradeDay(today);
            }
        }
    }
    
    @Override
    public LocalDate getNextTradeDayForPlan() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        
        if (isTradeDay(today) && currentTime.isBefore(MARKET_CLOSE)) {
            if (currentTime.isBefore(MARKET_OPEN)) {
                return today;
            } else {
                return getNextTradeDay(today);
            }
        } else {
            return getNextTradeDay(today);
        }
    }
    
    @Override
    public void batchInsert(List<TradeCalendar> calendars) {
        saveBatch(calendars);
    }
}
