package com.stock.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.stock.review.entity.Position;
import com.stock.review.entity.StockOperation;
import com.stock.review.entity.StockSelection;
import com.stock.review.entity.dto.PlanDTO;
import com.stock.review.mapper.PositionMapper;
import com.stock.review.mapper.StockOperationMapper;
import com.stock.review.mapper.StockSelectionMapper;
import com.stock.review.service.PlanService;
import com.stock.review.service.PositionService;
import com.stock.review.service.TradeCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {
    
    private final PositionMapper positionMapper;
    private final StockSelectionMapper stockSelectionMapper;
    private final StockOperationMapper stockOperationMapper;
    private final PositionService positionService;
    private final TradeCalendarService tradeCalendarService;
    
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
    public List<PlanDTO> getPlansByDateRange(LocalDate startDate, LocalDate endDate, String planStatus) {
        updateExpiredPlans();
        List<PlanDTO> plans = new ArrayList<>();
        
        LambdaQueryWrapper<Position> posWrapper = new LambdaQueryWrapper<Position>()
                .eq(Position::getStatus, "持仓中")
                .isNotNull(Position::getPlanType)
                .ge(Position::getPlanDate, startDate)
                .le(Position::getPlanDate, endDate);
        
        if (planStatus != null && !planStatus.isEmpty()) {
            posWrapper.eq(Position::getPlanStatus, planStatus);
        }
        
        posWrapper.orderByAsc(Position::getPlanDate);
        
        List<Position> positionPlans = positionMapper.selectList(posWrapper);
        
        for (Position pos : positionPlans) {
            PlanDTO plan = new PlanDTO();
            plan.setId(pos.getId());
            plan.setSourceType("position");
            plan.setStockName(pos.getStockName());
            plan.setStockCode(pos.getStockCode());
            plan.setPlanType(pos.getPlanType());
            plan.setPlanShares(pos.getPlanShares());
            plan.setPlanAmount(pos.getPlanAmount());
            plan.setPlanReason(pos.getPlanReason());
            plan.setPlanDate(pos.getPlanDate());
            plan.setExecuteTime(pos.getExecuteTime());
            plan.setPlanStatus(pos.getPlanStatus());
            plan.setTargetPrice(pos.getTargetPrice());
            plan.setStopLossPrice(pos.getStopLossPrice());
            plan.setProfitLossRatio(pos.getProfitLossRatio());
            plan.setExecuted(0);
            plan.setCanUpdate("待实施".equals(pos.getPlanStatus()) && 
                    pos.getPlanDate() != null && 
                    tradeCalendarService.canUpdatePlan(pos.getPlanDate()));
            
            if ("已实施".equals(pos.getPlanStatus())) {
                LambdaQueryWrapper<StockOperation> opWrapper = new LambdaQueryWrapper<StockOperation>()
                        .eq(StockOperation::getPositionId, pos.getId())
                        .eq(StockOperation::getOperationType, pos.getPlanType())
                        .orderByDesc(StockOperation::getOperationDate)
                        .last("LIMIT 1");
                StockOperation operation = stockOperationMapper.selectOne(opWrapper);
                if (operation != null) {
                    plan.setActualShares(operation.getOperationAmount() != null && operation.getOperationPrice() != null && operation.getOperationPrice().compareTo(BigDecimal.ZERO) > 0
                            ? operation.getOperationAmount().divide(operation.getOperationPrice(), 0, BigDecimal.ROUND_DOWN).intValue()
                            : null);
                    plan.setActualAmount(operation.getOperationAmount());
                    plan.setActualPrice(operation.getOperationPrice());
                }
            }
            
            plans.add(plan);
        }
        
        LambdaQueryWrapper<StockSelection> selWrapper = new LambdaQueryWrapper<StockSelection>()
                .isNotNull(StockSelection::getPlanDate)
                .ge(StockSelection::getPlanDate, startDate)
                .le(StockSelection::getPlanDate, endDate)
                .orderByAsc(StockSelection::getPlanDate);
        
        List<StockSelection> selectionPlans = stockSelectionMapper.selectList(selWrapper);
        
        LocalDate today = LocalDate.now();
        
        for (StockSelection sel : selectionPlans) {
            PlanDTO plan = new PlanDTO();
            plan.setId(sel.getId());
            plan.setSourceType("selection");
            plan.setStockName(sel.getStockName());
            plan.setStockCode(sel.getStockCode());
            plan.setPlanType("建仓");
            plan.setPlanShares(sel.getPlanShares());
            plan.setPlanAmount(sel.getPlanAmount());
            plan.setPlanReason(sel.getSelectionReason());
            plan.setPlanDate(sel.getPlanDate());
            plan.setExecuteTime(sel.getExecuteTime());
            plan.setTargetPrice(sel.getTargetPrice());
            plan.setStopLossPrice(sel.getStopLossPrice());
            plan.setProfitLossRatio(sel.getProfitLossRatio());
            plan.setExecuted(sel.getOperated());
            
            String selectionPlanStatus;
            if (sel.getOperated() != null && sel.getOperated() == 1) {
                selectionPlanStatus = "已实施";
            } else if (sel.getOperated() != null && sel.getOperated() == 2) {
                selectionPlanStatus = "未实施";
            } else {
                selectionPlanStatus = "待实施";
            }
            plan.setPlanStatus(selectionPlanStatus);
            plan.setCanUpdate("待实施".equals(selectionPlanStatus) && 
                    sel.getPlanDate() != null && 
                    tradeCalendarService.canUpdatePlan(sel.getPlanDate()));
            
            if ("已实施".equals(selectionPlanStatus) || "未实施".equals(selectionPlanStatus)) {
                LambdaQueryWrapper<StockOperation> opWrapper = new LambdaQueryWrapper<StockOperation>()
                        .eq(StockOperation::getStockCode, sel.getStockCode())
                        .eq(StockOperation::getOperationType, "建仓")
                        .eq(StockOperation::getOperationDate, sel.getPlanDate())
                        .last("LIMIT 1");
                StockOperation operation = stockOperationMapper.selectOne(opWrapper);
                if (operation != null) {
                    plan.setActualShares(operation.getOperationAmount() != null && operation.getOperationPrice() != null && operation.getOperationPrice().compareTo(BigDecimal.ZERO) > 0
                            ? operation.getOperationAmount().divide(operation.getOperationPrice(), 0, BigDecimal.ROUND_DOWN).intValue()
                            : null);
                    plan.setActualAmount(operation.getOperationAmount());
                    plan.setActualPrice(operation.getOperationPrice());
                }
            }
            
            if (planStatus == null || planStatus.isEmpty() || planStatus.equals(selectionPlanStatus)) {
                plans.add(plan);
            }
        }
        
        return plans;
    }
    
    @Override
    public void executePlan(Long id, String sourceType, Integer shares, BigDecimal price) {
        if ("position".equals(sourceType)) {
            Position position = positionMapper.selectById(id);
            if (position != null) {
                BigDecimal amount = price.multiply(new BigDecimal(shares));
                String planType = position.getPlanType();
                
                if ("加仓".equals(planType)) {
                    positionService.addPosition(id, amount, price);
                } else if ("减仓".equals(planType)) {
                    positionService.reducePosition(id, amount, price);
                } else if ("清仓".equals(planType)) {
                    positionService.clearPosition(id, price);
                }
                
                LambdaUpdateWrapper<Position> updateWrapper = new LambdaUpdateWrapper<Position>()
                        .eq(Position::getId, id)
                        .set(Position::getPlanStatus, "已实施");
                positionMapper.update(null, updateWrapper);
                
                StockOperation operation = new StockOperation();
                operation.setStockName(position.getStockName());
                operation.setStockCode(position.getStockCode());
                operation.setOperationType(planType);
                operation.setOperationAmount(amount);
                operation.setOperationPrice(price);
                operation.setOperationReason(position.getPlanReason());
                operation.setIsFollowRule(1);
                operation.setOperationDate(LocalDate.now());
                operation.setPositionId(id);
                stockOperationMapper.insert(operation);
            }
        } else if ("selection".equals(sourceType)) {
            StockSelection selection = stockSelectionMapper.selectById(id);
            if (selection != null) {
                BigDecimal amount = price.multiply(new BigDecimal(shares));
                
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
                
                StockOperation operation = new StockOperation();
                operation.setStockName(selection.getStockName());
                operation.setStockCode(selection.getStockCode());
                operation.setOperationType("建仓");
                operation.setOperationAmount(amount);
                operation.setOperationPrice(price);
                operation.setOperationReason(selection.getSelectionReason());
                operation.setIsFollowRule(1);
                operation.setOperationDate(LocalDate.now());
                operation.setPositionId(position.getId());
                stockOperationMapper.insert(operation);
                
                selection.setOperated(1);
                stockSelectionMapper.updateById(selection);
            }
        }
    }
    
    @Override
    public void deletePlan(Long id, String sourceType) {
        if ("position".equals(sourceType)) {
            Position position = positionMapper.selectById(id);
            if (position != null) {
                position.setPlanType(null);
                position.setPlanShares(null);
                position.setPlanAmount(null);
                position.setPlanReason(null);
                position.setPlanDate(null);
                position.setExecuteTime(null);
                position.setPlanStatus(null);
                positionMapper.updateById(position);
            }
        } else if ("selection".equals(sourceType)) {
            stockSelectionMapper.deleteById(id);
        }
    }
    
    @Override
    public void updateOverduePlans() {
        updateExpiredPlans();
        fixIncorrectlyExpiredPlans();
    }
    
    private void updateExpiredPlans() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        LocalTime marketOpen = LocalTime.of(9, 30);
        
        List<Position> allPendingPlans = positionMapper.selectList(
                new LambdaQueryWrapper<Position>()
                        .eq(Position::getStatus, "持仓中")
                        .eq(Position::getPlanStatus, "待实施")
                        .isNotNull(Position::getPlanDate));
        
        for (Position pos : allPendingPlans) {
            LocalDate planDate = pos.getPlanDate();
            
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
                LambdaQueryWrapper<StockOperation> opWrapper = new LambdaQueryWrapper<StockOperation>()
                        .eq(StockOperation::getPositionId, pos.getId())
                        .eq(StockOperation::getOperationType, pos.getPlanType())
                        .eq(StockOperation::getIsFollowRule, 0);
                Long existCount = stockOperationMapper.selectCount(opWrapper);
                
                if (existCount == 0) {
                    StockOperation operation = new StockOperation();
                    operation.setStockName(pos.getStockName());
                    operation.setStockCode(pos.getStockCode());
                    operation.setOperationType(pos.getPlanType());
                    operation.setOperationAmount(BigDecimal.ZERO);
                    operation.setOperationPrice(BigDecimal.ZERO);
                    operation.setOperationReason(pos.getPlanReason() != null ? pos.getPlanReason() : "未执行计划");
                    operation.setIsFollowRule(0);
                    operation.setOperationDate(planDate);
                    operation.setPositionId(pos.getId());
                    stockOperationMapper.insert(operation);
                }
                
                pos.setPlanStatus("未实施");
                positionMapper.updateById(pos);
            }
        }
        
        List<StockSelection> allPendingSelections = stockSelectionMapper.selectList(
                new LambdaQueryWrapper<StockSelection>()
                        .eq(StockSelection::getOperated, 0)
                        .isNotNull(StockSelection::getPlanDate));
        
        for (StockSelection sel : allPendingSelections) {
            LocalDate planDate = sel.getPlanDate();
            
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
                operation.setStockName(sel.getStockName());
                operation.setStockCode(sel.getStockCode());
                operation.setOperationType("建仓");
                operation.setOperationAmount(BigDecimal.ZERO);
                operation.setOperationPrice(BigDecimal.ZERO);
                operation.setOperationReason(sel.getSelectionReason() != null ? sel.getSelectionReason() : "未执行计划");
                operation.setIsFollowRule(0);
                operation.setOperationDate(planDate);
                stockOperationMapper.insert(operation);
                
                sel.setOperated(2);
                stockSelectionMapper.updateById(sel);
            }
        }
    }
    
    private void fixIncorrectlyExpiredPlans() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        LocalTime marketOpen = LocalTime.of(9, 30);
        
        List<Position> expiredPlans = positionMapper.selectList(
                new LambdaQueryWrapper<Position>()
                        .eq(Position::getStatus, "持仓中")
                        .eq(Position::getPlanStatus, "未实施")
                        .isNotNull(Position::getPlanDate));
        
        for (Position pos : expiredPlans) {
            LocalDate planDate = pos.getPlanDate();
            
            boolean shouldRestore = false;
            
            if (planDate.isEqual(today)) {
                shouldRestore = true;
            } else if (planDate.isBefore(today)) {
                LocalDate nextTradeDay = tradeCalendarService.getNextTradeDay(planDate);
                if (nextTradeDay != null) {
                    if (nextTradeDay.isAfter(today)) {
                        shouldRestore = true;
                    } else if (nextTradeDay.isEqual(today) && currentTime.isBefore(marketOpen)) {
                        shouldRestore = true;
                    }
                }
            }
            
            if (shouldRestore) {
                LambdaQueryWrapper<StockOperation> opWrapper = new LambdaQueryWrapper<StockOperation>()
                        .eq(StockOperation::getPositionId, pos.getId())
                        .eq(StockOperation::getOperationType, pos.getPlanType())
                        .eq(StockOperation::getIsFollowRule, 0);
                stockOperationMapper.delete(opWrapper);
                
                pos.setPlanStatus("待实施");
                positionMapper.updateById(pos);
            }
        }
        
        List<StockSelection> expiredSelections = stockSelectionMapper.selectList(
                new LambdaQueryWrapper<StockSelection>()
                        .eq(StockSelection::getOperated, 2)
                        .isNotNull(StockSelection::getPlanDate));
        
        for (StockSelection sel : expiredSelections) {
            LocalDate planDate = sel.getPlanDate();
            
            boolean shouldRestore = false;
            
            if (planDate.isEqual(today)) {
                shouldRestore = true;
            } else if (planDate.isBefore(today)) {
                LocalDate nextTradeDay = tradeCalendarService.getNextTradeDay(planDate);
                if (nextTradeDay != null) {
                    if (nextTradeDay.isAfter(today)) {
                        shouldRestore = true;
                    } else if (nextTradeDay.isEqual(today) && currentTime.isBefore(marketOpen)) {
                        shouldRestore = true;
                    }
                }
            }
            
            if (shouldRestore) {
                LambdaQueryWrapper<StockOperation> opWrapper = new LambdaQueryWrapper<StockOperation>()
                        .eq(StockOperation::getStockCode, sel.getStockCode())
                        .eq(StockOperation::getOperationType, "建仓")
                        .eq(StockOperation::getIsFollowRule, 0)
                        .eq(StockOperation::getOperationDate, planDate);
                stockOperationMapper.delete(opWrapper);
                
                sel.setOperated(0);
                stockSelectionMapper.updateById(sel);
            }
        }
    }
}
