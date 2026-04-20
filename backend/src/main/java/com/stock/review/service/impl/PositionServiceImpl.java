package com.stock.review.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.review.entity.Position;
import com.stock.review.entity.dto.PositionDTO;
import com.stock.review.entity.dto.PositionSummaryDTO;
import com.stock.review.mapper.PositionMapper;
import com.stock.review.service.PositionService;
import com.stock.review.service.TradePlanService;
import com.stock.review.utils.SecurityUtils;
import com.stock.review.utils.StockApiUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl extends ServiceImpl<PositionMapper, Position> implements PositionService {
    
    private final StockApiUtil stockApiUtil;
    private final TradePlanService tradePlanService;
    
    @Override
    public Position createPosition(String stockName, String stockCode, BigDecimal amount, BigDecimal price) {
        Position position = new Position();
        position.setUserId(SecurityUtils.getCurrentUserId());
        position.setStockName(stockName);
        position.setStockCode(stockCode);
        position.setHoldAmount(amount);
        position.setCostPrice(price);
        position.setHoldShares(amount.divide(price, 0, RoundingMode.DOWN).intValue());
        position.setStatus("持仓中");
        
        BigDecimal currentPrice = stockApiUtil.getCurrentPrice(stockCode);
        position.setCurrentPrice(currentPrice);
        
        calculateProfitLoss(position);
        save(position);
        return position;
    }
    
    @Override
    public Position addPosition(Long positionId, BigDecimal amount, BigDecimal price) {
        Position position = getById(positionId);
        if (position == null) {
            throw new IllegalArgumentException("持仓记录不存在");
        }
        
        int newShares = amount.divide(price, 0, RoundingMode.DOWN).intValue();
        int totalShares = position.getHoldShares() + newShares;
        BigDecimal totalAmount = position.getHoldAmount().add(amount);
        BigDecimal avgCost = totalAmount.divide(new BigDecimal(totalShares), 2, RoundingMode.HALF_UP);
        
        position.setHoldShares(totalShares);
        position.setHoldAmount(totalAmount);
        position.setCostPrice(avgCost);
        
        BigDecimal currentPrice = stockApiUtil.getCurrentPrice(position.getStockCode());
        position.setCurrentPrice(currentPrice);
        
        calculateProfitLoss(position);
        updateById(position);
        return position;
    }
    
    @Override
    public Position reducePosition(Long positionId, BigDecimal amount, BigDecimal price) {
        Position position = getById(positionId);
        if (position == null) {
            throw new IllegalArgumentException("持仓记录不存在");
        }
        
        int reduceShares = amount.divide(price, 0, RoundingMode.DOWN).intValue();
        int remainShares = position.getHoldShares() - reduceShares;
        
        if (remainShares <= 0) {
            return clearPosition(positionId, price);
        }
        
        BigDecimal remainAmount = position.getCostPrice().multiply(new BigDecimal(remainShares));
        position.setHoldShares(remainShares);
        position.setHoldAmount(remainAmount);
        
        BigDecimal currentPrice = stockApiUtil.getCurrentPrice(position.getStockCode());
        position.setCurrentPrice(currentPrice);
        
        calculateProfitLoss(position);
        updateById(position);
        return position;
    }
    
    @Override
    public Position clearPosition(Long positionId, BigDecimal price) {
        Position position = getById(positionId);
        if (position == null) {
            throw new IllegalArgumentException("持仓记录不存在");
        }
        
        BigDecimal clearAmount = price.multiply(new BigDecimal(position.getHoldShares()));
        BigDecimal profitLoss = clearAmount.subtract(position.getHoldAmount());
        
        position.setStatus("已清仓");
        position.setClearPrice(price);
        position.setClearProfitLoss(profitLoss);
        position.setClearDate(LocalDate.now());
        position.setHoldShares(0);
        position.setHoldAmount(BigDecimal.ZERO);
        
        updateById(position);
        return position;
    }
    
    @Override
    public Page<Position> pageList(Integer pageNum, Integer pageSize, String status, String sortProp, String sortOrder) {
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(Position::getUserId, SecurityUtils.getCurrentUserId());
        
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(Position::getStatus, status);
        }
        
        if (StrUtil.isNotBlank(sortProp) && StrUtil.isNotBlank(sortOrder)) {
            boolean isAsc = "ascending".equals(sortOrder);
            switch (sortProp) {
                case "stockName":
                    wrapper.orderBy(true, isAsc, Position::getStockName);
                    break;
                case "holdAmount":
                    wrapper.orderBy(true, isAsc, Position::getHoldAmount);
                    break;
                case "holdShares":
                    wrapper.orderBy(true, isAsc, Position::getHoldShares);
                    break;
                case "costPrice":
                    wrapper.orderBy(true, isAsc, Position::getCostPrice);
                    break;
                case "currentPrice":
                    wrapper.orderBy(true, isAsc, Position::getCurrentPrice);
                    break;
                case "targetPrice":
                    wrapper.orderBy(true, isAsc, Position::getTargetPrice);
                    break;
                case "stopLossPrice":
                    wrapper.orderBy(true, isAsc, Position::getStopLossPrice);
                    break;
                case "profitLoss":
                    wrapper.orderBy(true, isAsc, Position::getProfitLoss);
                    break;
                case "profitLossPercent":
                    wrapper.orderBy(true, isAsc, Position::getProfitLossPercent);
                    break;
                case "profitLossRatio":
                    wrapper.orderBy(true, isAsc, Position::getProfitLossRatio);
                    break;
                default:
                    wrapper.orderByDesc(Position::getCreateTime);
            }
        } else {
            wrapper.orderByDesc(Position::getCreateTime);
        }
        
        return page(new Page<>(pageNum, pageSize), wrapper);
    }
    
    @Override
    public PositionSummaryDTO getSummary() {
        List<Position> positions = list(new LambdaQueryWrapper<Position>()
                .eq(Position::getUserId, SecurityUtils.getCurrentUserId())
                .eq(Position::getStatus, "持仓中"));
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalProfitLoss = BigDecimal.ZERO;
        
        for (Position position : positions) {
            if (position.getHoldAmount() != null) {
                totalAmount = totalAmount.add(position.getHoldAmount());
            }
            if (position.getProfitLoss() != null) {
                totalProfitLoss = totalProfitLoss.add(position.getProfitLoss());
            }
        }
        
        PositionSummaryDTO dto = new PositionSummaryDTO();
        dto.setTotalCount(positions.size());
        dto.setTotalAmount(totalAmount);
        dto.setTotalProfitLoss(totalProfitLoss);
        
        if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            dto.setTotalProfitLossPercent(totalProfitLoss.divide(totalAmount, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")));
        } else {
            dto.setTotalProfitLossPercent(BigDecimal.ZERO);
        }
        
        return dto;
    }
    
    @Override
    public void refreshPrices() {
        List<Position> positions = getActivePositions();
        
        if (positions.isEmpty()) {
            return;
        }
        
        List<String> stockCodes = new java.util.ArrayList<>();
        for (Position position : positions) {
            stockCodes.add(position.getStockCode());
        }
        
        Map<String, BigDecimal> prices = stockApiUtil.batchGetCurrentPrice(stockCodes);
        
        for (Position position : positions) {
            BigDecimal currentPrice = prices.get(position.getStockCode());
            if (currentPrice != null) {
                position.setCurrentPrice(currentPrice);
                calculateProfitLoss(position);
            }
        }
        
        updateBatchById(positions);
    }
    
    @Override
    public Position getByStockCode(String stockCode) {
        return getOne(new LambdaQueryWrapper<Position>()
                .eq(Position::getUserId, SecurityUtils.getCurrentUserId())
                .eq(Position::getStockCode, stockCode)
                .eq(Position::getStatus, "持仓中"));
    }
    
    @Override
    public List<Position> getActivePositions() {
        return list(new LambdaQueryWrapper<Position>()
                .eq(Position::getUserId, SecurityUtils.getCurrentUserId())
                .eq(Position::getStatus, "持仓中"));
    }
    
    private void calculateProfitLoss(Position position) {
        if (position.getCurrentPrice() == null || position.getCostPrice() == null) {
            return;
        }
        
        BigDecimal currentAmount = position.getCurrentPrice().multiply(new BigDecimal(position.getHoldShares()));
        position.setHoldAmount(currentAmount);
        
        BigDecimal costAmount = position.getCostPrice().multiply(new BigDecimal(position.getHoldShares()));
        BigDecimal profitLoss = currentAmount.subtract(costAmount);
        position.setProfitLoss(profitLoss);
        
        if (costAmount.compareTo(BigDecimal.ZERO) > 0) {
            position.setProfitLossPercent(profitLoss.divide(costAmount, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")));
        }
        
        if (position.getTargetPrice() != null && position.getStopLossPrice() != null 
                && position.getStopLossPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal expectedProfit = position.getTargetPrice().subtract(position.getCurrentPrice());
            BigDecimal expectedLoss = position.getCurrentPrice().subtract(position.getStopLossPrice());
            
            if (expectedLoss.compareTo(BigDecimal.ZERO) > 0) {
                position.setProfitLossRatio(expectedProfit.divide(expectedLoss, 2, RoundingMode.HALF_UP));
            }
        }
        
        checkAlertStatus(position);
    }
    
    private void checkAlertStatus(Position position) {
        if (!"closed".equals(position.getAlertStatus())) {
            BigDecimal currentPrice = position.getCurrentPrice();
            BigDecimal stopLossPrice = position.getStopLossPrice();
            BigDecimal targetPrice = position.getTargetPrice();
            
            if (stopLossPrice != null && stopLossPrice.compareTo(BigDecimal.ZERO) > 0 
                    && currentPrice.compareTo(stopLossPrice) <= 0) {
                position.setAlertStatus("stop_loss");
            } else if (targetPrice != null && targetPrice.compareTo(BigDecimal.ZERO) > 0 
                    && currentPrice.compareTo(targetPrice) >= 0) {
                position.setAlertStatus("take_profit");
            } else {
                position.setAlertStatus(null);
            }
        }
    }
    
    @Override
    public Position initPosition(PositionDTO dto) {
        Position existPosition = getByStockCode(dto.getStockCode());
        if (existPosition != null) {
            throw new IllegalArgumentException("该股票已存在持仓记录");
        }
        
        Position position = new Position();
        position.setUserId(SecurityUtils.getCurrentUserId());
        position.setStockName(dto.getStockName());
        position.setStockCode(dto.getStockCode());
        position.setHoldAmount(dto.getHoldAmount());
        position.setHoldShares(dto.getHoldShares());
        position.setCostPrice(dto.getCostPrice());
        position.setTargetPrice(dto.getTargetPrice());
        position.setStopLossPrice(dto.getStopLossPrice());
        position.setStatus("持仓中");
        
        BigDecimal currentPrice = stockApiUtil.getCurrentPrice(dto.getStockCode());
        position.setCurrentPrice(currentPrice);
        
        calculateProfitLoss(position);
        save(position);
        return position;
    }
    
    @Override
    public void savePlan(com.stock.review.controller.PositionController.PositionPlanDTO dto) {
        Position position = getById(dto.getId());
        if (position == null) {
            throw new IllegalArgumentException("持仓记录不存在");
        }
        
        position.setPlanType(dto.getPlanType());
        position.setPlanShares(dto.getPlanShares());
        position.setPlanAmount(dto.getPlanAmount());
        position.setPlanReason(dto.getPlanReason());
        position.setExecuteTime(dto.getExecuteTime());
        position.setPlanStatus("待实施");
        position.setPlanCreateTime(java.time.LocalDateTime.now());
        
        if ("减仓".equals(dto.getPlanType()) || "清仓".equals(dto.getPlanType())) {
            position.setAlertStatus("closed");
        }
        
        LocalDate planDate = null;
        if (dto.getPlanDate() != null && !dto.getPlanDate().isEmpty()) {
            planDate = LocalDate.parse(dto.getPlanDate());
        } else {
            planDate = LocalDate.now();
        }
        position.setPlanDate(planDate);
        
        updateById(position);
        
        tradePlanService.syncFromPosition(
            position.getId(),
            position.getStockName(),
            position.getStockCode(),
            dto.getPlanType(),
            dto.getPlanShares(),
            dto.getPlanAmount(),
            dto.getPlanReason(),
            planDate,
            dto.getExecuteTime()
        );
    }
    
    @Override
    public Position updatePosition(PositionDTO dto) {
        Position position = getById(dto.getId());
        if (position == null) {
            throw new IllegalArgumentException("持仓记录不存在");
        }
        
        position.setStockName(dto.getStockName());
        position.setStockCode(dto.getStockCode());
        position.setHoldShares(dto.getHoldShares());
        position.setCostPrice(dto.getCostPrice());
        position.setHoldAmount(dto.getHoldAmount());
        position.setTargetPrice(dto.getTargetPrice());
        position.setStopLossPrice(dto.getStopLossPrice());
        
        BigDecimal currentPrice = stockApiUtil.getCurrentPrice(dto.getStockCode());
        position.setCurrentPrice(currentPrice);
        
        calculateProfitLoss(position);
        updateById(position);
        return position;
    }
    
    @Override
    public void deletePosition(Long id) {
        Position position = getById(id);
        if (position == null) {
            throw new IllegalArgumentException("持仓记录不存在");
        }
        
        removeById(id);
    }
    
    @Override
    public void closeAlert(Long id) {
        Position position = getById(id);
        if (position == null) {
            throw new IllegalArgumentException("持仓记录不存在");
        }
        
        position.setAlertStatus("closed");
        updateById(position);
    }
}
