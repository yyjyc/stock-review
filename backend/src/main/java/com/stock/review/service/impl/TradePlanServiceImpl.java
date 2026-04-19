package com.stock.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.review.entity.Position;
import com.stock.review.entity.StockOperation;
import com.stock.review.entity.StockSelection;
import com.stock.review.entity.TradePlan;
import com.stock.review.entity.dto.PlanDTO;
import com.stock.review.mapper.PositionMapper;
import com.stock.review.mapper.StockOperationMapper;
import com.stock.review.mapper.StockSelectionMapper;
import com.stock.review.mapper.TradePlanMapper;
import com.stock.review.service.PositionService;
import com.stock.review.service.TradeCalendarService;
import com.stock.review.service.TradePlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TradePlanServiceImpl extends ServiceImpl<TradePlanMapper, TradePlan> implements TradePlanService {
    
    private final TradeCalendarService tradeCalendarService;
    private final StockOperationMapper stockOperationMapper;
    private final PositionMapper positionMapper;
    private final PositionService positionService;
    private final StockSelectionMapper stockSelectionMapper;
    
    public TradePlanServiceImpl(
            TradeCalendarService tradeCalendarService,
            StockOperationMapper stockOperationMapper,
            PositionMapper positionMapper,
            @Lazy PositionService positionService,
            StockSelectionMapper stockSelectionMapper) {
        this.tradeCalendarService = tradeCalendarService;
        this.stockOperationMapper = stockOperationMapper;
        this.positionMapper = positionMapper;
        this.positionService = positionService;
        this.stockSelectionMapper = stockSelectionMapper;
    }
    
    @Override
    public List<PlanDTO> getPlansByDateRange(LocalDate startDate, LocalDate endDate, String planStatus) {
        updateExpiredPlans();
        
        List<PlanDTO> plans = new ArrayList<>();
        
        LambdaQueryWrapper<TradePlan> wrapper = new LambdaQueryWrapper<TradePlan>()
                .ge(TradePlan::getPlanDate, startDate)
                .le(TradePlan::getPlanDate, endDate)
                .orderByAsc(TradePlan::getPlanDate);
        
        if (planStatus != null && !planStatus.isEmpty()) {
            wrapper.eq(TradePlan::getPlanStatus, planStatus);
        }
        
        List<TradePlan> tradePlans = list(wrapper);
        
        for (TradePlan tp : tradePlans) {
            PlanDTO plan = convertToDTO(tp);
            plans.add(plan);
        }
        
        return plans;
    }
    
    @Override
    public List<PlanDTO> getTodayPlans(String planStatus) {
        updateExpiredPlans();
        LocalDate reviewDate = tradeCalendarService.getCurrentReviewTradeDate();
        if (reviewDate == null) {
            reviewDate = LocalDate.now();
        }
        return getPlansByDateRange(reviewDate, reviewDate, planStatus);
    }
    
    @Override
    public void executePlan(Long id, Integer shares, BigDecimal price) {
        TradePlan plan = getById(id);
        if (plan == null) {
            throw new RuntimeException("计划不存在");
        }
        
        BigDecimal amount = price.multiply(new BigDecimal(shares));
        
        if ("position".equals(plan.getSourceType()) && plan.getSourceId() != null) {
            Position position = positionMapper.selectById(plan.getSourceId());
            if (position != null) {
                String planType = plan.getPlanType();
                
                if ("加仓".equals(planType)) {
                    positionService.addPosition(plan.getSourceId(), amount, price);
                } else if ("减仓".equals(planType)) {
                    positionService.reducePosition(plan.getSourceId(), amount, price);
                } else if ("清仓".equals(planType)) {
                    positionService.clearPosition(plan.getSourceId(), price);
                }
                
                position = positionMapper.selectById(plan.getSourceId());
                if (position != null) {
                    position.setPlanStatus("已实施");
                    positionMapper.updateById(position);
                }
            }
        } else if ("selection".equals(plan.getSourceType()) && plan.getSourceId() != null) {
            StockSelection selection = stockSelectionMapper.selectById(plan.getSourceId());
            if (selection != null) {
                Position position = new Position();
                position.setStockName(selection.getStockName());
                position.setStockCode(selection.getStockCode());
                position.setHoldShares(shares);
                position.setCostPrice(price);
                position.setHoldAmount(amount);
                position.setTargetPrice(selection.getTargetPrice());
                position.setStopLossPrice(selection.getStopLossPrice());
                position.setStatus("持仓中");
                positionMapper.insert(position);
                
                selection.setOperated(1);
                stockSelectionMapper.updateById(selection);
            }
        }
        
        StockOperation operation = new StockOperation();
        operation.setStockName(plan.getStockName());
        operation.setStockCode(plan.getStockCode());
        operation.setOperationType(plan.getPlanType());
        operation.setOperationAmount(amount);
        operation.setOperationPrice(price);
        operation.setOperationReason(plan.getPlanReason() != null ? plan.getPlanReason() : "执行计划");
        operation.setIsFollowRule(1);
        operation.setOperationDate(plan.getPlanDate());
        if ("position".equals(plan.getSourceType()) && plan.getSourceId() != null) {
            operation.setPositionId(plan.getSourceId());
        }
        stockOperationMapper.insert(operation);
        
        plan.setPlanStatus("已实施");
        plan.setActualShares(shares);
        plan.setActualAmount(amount);
        plan.setActualPrice(price);
        updateById(plan);
    }
    
    @Override
    public void markAsNotExecuted(Long id) {
        TradePlan plan = getById(id);
        if (plan == null) {
            throw new RuntimeException("计划不存在");
        }
        
        if ("position".equals(plan.getSourceType()) && plan.getSourceId() != null) {
            Position position = positionMapper.selectById(plan.getSourceId());
            if (position != null) {
                position.setPlanStatus("未实施");
                positionMapper.updateById(position);
            }
        } else if ("selection".equals(plan.getSourceType()) && plan.getSourceId() != null) {
            StockSelection selection = stockSelectionMapper.selectById(plan.getSourceId());
            if (selection != null) {
                selection.setOperated(2);
                stockSelectionMapper.updateById(selection);
            }
        }
        
        StockOperation operation = new StockOperation();
        operation.setStockName(plan.getStockName());
        operation.setStockCode(plan.getStockCode());
        operation.setOperationType(plan.getPlanType());
        operation.setOperationAmount(BigDecimal.ZERO);
        operation.setOperationPrice(BigDecimal.ZERO);
        operation.setOperationReason(plan.getPlanReason() != null ? plan.getPlanReason() : "未执行计划");
        operation.setIsFollowRule(0);
        operation.setOperationDate(plan.getPlanDate());
        if ("position".equals(plan.getSourceType()) && plan.getSourceId() != null) {
            operation.setPositionId(plan.getSourceId());
        }
        stockOperationMapper.insert(operation);
        
        plan.setPlanStatus("未实施");
        updateById(plan);
    }
    
    @Override
    public void updateExpiredPlans() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        LocalTime marketOpen = LocalTime.of(9, 30);
        
        List<TradePlan> pendingPlans = list(new LambdaQueryWrapper<TradePlan>()
                .eq(TradePlan::getPlanStatus, "待实施")
                .isNotNull(TradePlan::getPlanDate));
        
        for (TradePlan plan : pendingPlans) {
            LocalDate planDate = plan.getPlanDate();
            
            boolean shouldExpire = false;
            
            if (planDate.isBefore(today)) {
                LocalDate nextTradeDay = tradeCalendarService.getNextTradeDay(planDate);
                if (nextTradeDay != null) {
                    if (nextTradeDay.isBefore(today)) {
                        shouldExpire = true;
                    } else if (nextTradeDay.isEqual(today) && currentTime.isAfter(marketOpen)) {
                        shouldExpire = true;
                    }
                }
            }
            
            if (shouldExpire) {
                StockOperation operation = new StockOperation();
                operation.setStockName(plan.getStockName());
                operation.setStockCode(plan.getStockCode());
                operation.setOperationType(plan.getPlanType());
                operation.setOperationAmount(BigDecimal.ZERO);
                operation.setOperationPrice(BigDecimal.ZERO);
                operation.setOperationReason(plan.getPlanReason() != null ? plan.getPlanReason() : "未执行计划");
                operation.setIsFollowRule(0);
                operation.setOperationDate(planDate);
                stockOperationMapper.insert(operation);
                
                plan.setPlanStatus("未实施");
                updateById(plan);
            }
        }
    }
    
    @Override
    public void syncFromPosition(Long positionId, String stockName, String stockCode, String planType, Integer planShares, BigDecimal planAmount, String planReason, LocalDate planDate, String executeTime) {
        LambdaQueryWrapper<TradePlan> wrapper = new LambdaQueryWrapper<TradePlan>()
                .eq(TradePlan::getSourceType, "position")
                .eq(TradePlan::getSourceId, positionId);
        
        TradePlan existing = getOne(wrapper);
        
        if (planDate == null) {
            if (existing != null) {
                removeById(existing.getId());
            }
            return;
        }
        
        if (existing == null) {
            existing = new TradePlan();
            existing.setSourceType("position");
            existing.setSourceId(positionId);
            existing.setPlanStatus("待实施");
        }
        
        existing.setStockName(stockName);
        existing.setStockCode(stockCode);
        existing.setPlanType(planType);
        existing.setPlanShares(planShares);
        existing.setPlanAmount(planAmount);
        existing.setPlanReason(planReason);
        existing.setPlanDate(planDate);
        existing.setExecuteTime(executeTime);
        
        saveOrUpdate(existing);
    }
    
    @Override
    public void syncFromSelection(Long selectionId, String stockName, String stockCode, Integer planShares, BigDecimal planAmount, String planReason, LocalDate planDate, String executeTime, BigDecimal targetPrice, BigDecimal stopLossPrice) {
        LambdaQueryWrapper<TradePlan> wrapper = new LambdaQueryWrapper<TradePlan>()
                .eq(TradePlan::getSourceType, "selection")
                .eq(TradePlan::getSourceId, selectionId);
        
        TradePlan existing = getOne(wrapper);
        
        if (planDate == null) {
            if (existing != null) {
                removeById(existing.getId());
            }
            return;
        }
        
        if (existing == null) {
            existing = new TradePlan();
            existing.setSourceType("selection");
            existing.setSourceId(selectionId);
            existing.setPlanStatus("待实施");
        }
        
        existing.setStockName(stockName);
        existing.setStockCode(stockCode);
        existing.setPlanType("建仓");
        existing.setPlanShares(planShares);
        existing.setPlanAmount(planAmount);
        existing.setPlanReason(planReason);
        existing.setPlanDate(planDate);
        existing.setExecuteTime(executeTime);
        existing.setTargetPrice(targetPrice);
        existing.setStopLossPrice(stopLossPrice);
        
        saveOrUpdate(existing);
    }
    
    @Override
    public void deleteBySource(String sourceType, Long sourceId) {
        remove(new LambdaQueryWrapper<TradePlan>()
                .eq(TradePlan::getSourceType, sourceType)
                .eq(TradePlan::getSourceId, sourceId));
    }
    
    @Override
    public Page<PlanDTO> pageList(Integer pageNum, Integer pageSize, String startDate, String endDate, String planStatus, String stockName) {
        updateExpiredPlans();
        
        Page<TradePlan> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TradePlan> wrapper = new LambdaQueryWrapper<>();
        
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(TradePlan::getPlanDate, LocalDate.parse(startDate));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(TradePlan::getPlanDate, LocalDate.parse(endDate));
        }
        if (planStatus != null && !planStatus.isEmpty()) {
            wrapper.eq(TradePlan::getPlanStatus, planStatus);
        }
        if (stockName != null && !stockName.isEmpty()) {
            wrapper.like(TradePlan::getStockName, stockName);
        }
        
        wrapper.orderByDesc(TradePlan::getPlanDate);
        
        Page<TradePlan> result = page(page, wrapper);
        
        Page<PlanDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<PlanDTO> dtoList = new ArrayList<>();
        for (TradePlan tp : result.getRecords()) {
            dtoList.add(convertToDTO(tp));
        }
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }
    
    private PlanDTO convertToDTO(TradePlan tp) {
        PlanDTO plan = new PlanDTO();
        plan.setId(tp.getId());
        plan.setSourceType(tp.getSourceType());
        plan.setStockName(tp.getStockName());
        plan.setStockCode(tp.getStockCode());
        plan.setPlanType(tp.getPlanType());
        plan.setPlanShares(tp.getPlanShares());
        plan.setPlanAmount(tp.getPlanAmount());
        plan.setPlanReason(tp.getPlanReason());
        plan.setPlanDate(tp.getPlanDate());
        plan.setExecuteTime(tp.getExecuteTime());
        plan.setPlanStatus(tp.getPlanStatus());
        plan.setTargetPrice(tp.getTargetPrice());
        plan.setStopLossPrice(tp.getStopLossPrice());
        plan.setActualShares(tp.getActualShares());
        plan.setActualAmount(tp.getActualAmount());
        plan.setActualPrice(tp.getActualPrice());
        plan.setCanUpdate("待实施".equals(tp.getPlanStatus()) && 
                tp.getPlanDate() != null && 
                tradeCalendarService.canUpdatePlan(tp.getPlanDate()));
        return plan;
    }
}
