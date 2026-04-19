package com.stock.review.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.review.entity.Position;
import com.stock.review.entity.StockSelection;
import com.stock.review.entity.dto.PositionDTO;
import com.stock.review.entity.dto.StockSelectionDTO;
import com.stock.review.mapper.StockSelectionMapper;
import com.stock.review.service.PositionService;
import com.stock.review.service.StockSelectionService;
import com.stock.review.service.TradePlanService;
import com.stock.review.utils.StockApiUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockSelectionServiceImpl extends ServiceImpl<StockSelectionMapper, StockSelection> implements StockSelectionService {
    
    private final StockApiUtil stockApiUtil;
    private final PositionService positionService;
    private final TradePlanService tradePlanService;
    
    @Override
    public StockSelection saveOrUpdate(StockSelectionDTO dto) {
        StockSelection entity = new StockSelection();
        if (dto.getId() != null) {
            entity = getById(dto.getId());
        }
        
        BeanUtil.copyProperties(dto, entity);
        entity.setOperated(0);
        
        if (entity.getPlanDate() == null && entity.getSelectionDate() != null) {
            entity.setPlanDate(entity.getSelectionDate());
        }
        
        BigDecimal currentPrice = stockApiUtil.getCurrentPrice(dto.getStockCode());
        entity.setCurrentPrice(currentPrice);
        
        calculateProfitLossRatio(entity);
        
        saveOrUpdate(entity);
        
        tradePlanService.syncFromSelection(
            entity.getId(),
            entity.getStockName(),
            entity.getStockCode(),
            entity.getPlanShares(),
            entity.getPlanAmount(),
            entity.getSelectionReason(),
            entity.getPlanDate(),
            entity.getExecuteTime(),
            entity.getTargetPrice(),
            entity.getStopLossPrice()
        );
        
        return entity;
    }
    
    @Override
    public Page<StockSelection> pageList(Integer pageNum, Integer pageSize) {
        return page(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<StockSelection>()
                .orderByDesc(StockSelection::getSelectionDate));
    }
    
    @Override
    public void deleteById(Long id) {
        removeById(id);
    }
    
    @Override
    @Transactional
    public void markAsOperated(Long id) {
        StockSelection entity = getById(id);
        if (entity != null) {
            entity.setOperated(1);
            updateById(entity);
            
            Position existingPosition = positionService.getByStockCode(entity.getStockCode());
            if (existingPosition == null) {
                PositionDTO positionDTO = new PositionDTO();
                positionDTO.setStockName(entity.getStockName());
                positionDTO.setStockCode(entity.getStockCode());
                positionDTO.setTargetPrice(entity.getTargetPrice());
                positionDTO.setStopLossPrice(entity.getStopLossPrice());
                positionDTO.setHoldShares(0);
                positionDTO.setCostPrice(BigDecimal.ZERO);
                positionDTO.setHoldAmount(BigDecimal.ZERO);
                positionDTO.setStatus("active");
                
                positionService.initPosition(positionDTO);
            }
        }
    }
    
    @Override
    public void refreshPrices() {
        List<StockSelection> selections = list(new LambdaQueryWrapper<StockSelection>()
                .eq(StockSelection::getOperated, 0));
        
        if (selections.isEmpty()) {
            return;
        }
        
        List<String> stockCodes = new java.util.ArrayList<>();
        for (StockSelection selection : selections) {
            stockCodes.add(selection.getStockCode());
        }
        
        Map<String, BigDecimal> prices = stockApiUtil.batchGetCurrentPrice(stockCodes);
        
        for (StockSelection selection : selections) {
            BigDecimal currentPrice = prices.get(selection.getStockCode());
            if (currentPrice != null) {
                selection.setCurrentPrice(currentPrice);
                calculateProfitLossRatio(selection);
            }
        }
        
        updateBatchById(selections);
    }
    
    private void calculateProfitLossRatio(StockSelection entity) {
        if (entity.getCurrentPrice() == null || entity.getTargetPrice() == null || entity.getStopLossPrice() == null) {
            return;
        }
        
        if (entity.getCurrentPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        
        BigDecimal targetGain = entity.getTargetPrice().subtract(entity.getCurrentPrice())
                .divide(entity.getCurrentPrice(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        
        BigDecimal stopLoss = entity.getCurrentPrice().subtract(entity.getStopLossPrice())
                .divide(entity.getCurrentPrice(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        
        if (stopLoss.compareTo(BigDecimal.ZERO) > 0) {
            entity.setProfitLossRatio(targetGain.divide(stopLoss, 2, RoundingMode.HALF_UP));
        }
    }
}
