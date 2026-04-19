package com.stock.review.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.review.entity.Position;
import com.stock.review.entity.StockOperation;
import com.stock.review.entity.dto.OperationStatisticsDTO;
import com.stock.review.entity.dto.StockOperationDTO;
import com.stock.review.mapper.StockOperationMapper;
import com.stock.review.service.PositionService;
import com.stock.review.service.StockOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class StockOperationServiceImpl extends ServiceImpl<StockOperationMapper, StockOperation> implements StockOperationService {
    
    private final PositionService positionService;
    
    @Override
    @Transactional
    public StockOperation saveOrUpdate(StockOperationDTO dto) {
        StockOperation entity = new StockOperation();
        if (dto.getId() != null) {
            entity = getById(dto.getId());
        }
        
        BeanUtil.copyProperties(dto, entity);
        
        Long positionId = dto.getPositionId();
        String operationType = dto.getOperationType();
        
        switch (operationType) {
            case "建仓":
                Position newPosition = positionService.createPosition(
                        dto.getStockName(),
                        dto.getStockCode(),
                        dto.getOperationAmount(),
                        dto.getOperationPrice()
                );
                positionId = newPosition.getId();
                break;
            case "加仓":
                if (positionId == null) {
                    Position existingPosition = positionService.getByStockCode(dto.getStockCode());
                    if (existingPosition != null) {
                        positionId = existingPosition.getId();
                    }
                }
                if (positionId != null) {
                    positionService.addPosition(positionId, dto.getOperationAmount(), dto.getOperationPrice());
                }
                break;
            case "减仓":
                if (positionId == null) {
                    Position existingPosition = positionService.getByStockCode(dto.getStockCode());
                    if (existingPosition != null) {
                        positionId = existingPosition.getId();
                    }
                }
                if (positionId != null) {
                    positionService.reducePosition(positionId, dto.getOperationAmount(), dto.getOperationPrice());
                }
                break;
            case "清仓":
                if (positionId == null) {
                    Position existingPosition = positionService.getByStockCode(dto.getStockCode());
                    if (existingPosition != null) {
                        positionId = existingPosition.getId();
                    }
                }
                if (positionId != null) {
                    positionService.clearPosition(positionId, dto.getOperationPrice());
                }
                break;
        }
        
        entity.setPositionId(positionId);
        saveOrUpdate(entity);
        return entity;
    }
    
    @Override
    public Page<StockOperation> pageList(Integer pageNum, Integer pageSize, String startDate, String endDate, String stockName, Integer followRule) {
        LambdaQueryWrapper<StockOperation> wrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(startDate)) {
            wrapper.ge(StockOperation::getOperationDate, startDate);
        }
        if (StrUtil.isNotBlank(endDate)) {
            wrapper.le(StockOperation::getOperationDate, endDate);
        }
        if (StrUtil.isNotBlank(stockName)) {
            wrapper.like(StockOperation::getStockName, stockName);
        }
        if (followRule != null) {
            wrapper.eq(StockOperation::getIsFollowRule, followRule);
        }
        
        wrapper.orderByDesc(StockOperation::getOperationDate);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }
    
    @Override
    public OperationStatisticsDTO getStatistics() {
        OperationStatisticsDTO dto = new OperationStatisticsDTO();
        
        Long totalCount = count();
        Long followRuleCount = count(new LambdaQueryWrapper<StockOperation>()
                .eq(StockOperation::getIsFollowRule, 1));
        Long notFollowRuleCount = totalCount - followRuleCount;
        
        dto.setTotalCount(totalCount);
        dto.setFollowRuleCount(followRuleCount);
        dto.setNotFollowRuleCount(notFollowRuleCount);
        
        if (totalCount > 0) {
            dto.setFollowRulePercent(new BigDecimal(followRuleCount)
                    .divide(new BigDecimal(totalCount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")));
            dto.setNotFollowRulePercent(new BigDecimal(notFollowRuleCount)
                    .divide(new BigDecimal(totalCount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")));
        } else {
            dto.setFollowRulePercent(BigDecimal.ZERO);
            dto.setNotFollowRulePercent(BigDecimal.ZERO);
        }
        
        return dto;
    }
    
    @Override
    public OperationStatisticsDTO getStatisticsByDateRange(String startDate, String endDate) {
        OperationStatisticsDTO dto = new OperationStatisticsDTO();
        
        LambdaQueryWrapper<StockOperation> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(startDate)) {
            wrapper.ge(StockOperation::getOperationDate, startDate);
        }
        if (StrUtil.isNotBlank(endDate)) {
            wrapper.le(StockOperation::getOperationDate, endDate);
        }
        
        Long totalCount = count(wrapper);
        Long followRuleCount = count(new LambdaQueryWrapper<StockOperation>()
                .eq(StockOperation::getIsFollowRule, 1)
                .ge(StrUtil.isNotBlank(startDate), StockOperation::getOperationDate, startDate)
                .le(StrUtil.isNotBlank(endDate), StockOperation::getOperationDate, endDate));
        Long notFollowRuleCount = totalCount - followRuleCount;
        
        dto.setTotalCount(totalCount);
        dto.setFollowRuleCount(followRuleCount);
        dto.setNotFollowRuleCount(notFollowRuleCount);
        
        if (totalCount > 0) {
            dto.setFollowRulePercent(new BigDecimal(followRuleCount)
                    .divide(new BigDecimal(totalCount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")));
            dto.setNotFollowRulePercent(new BigDecimal(notFollowRuleCount)
                    .divide(new BigDecimal(totalCount), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")));
        } else {
            dto.setFollowRulePercent(BigDecimal.ZERO);
            dto.setNotFollowRulePercent(BigDecimal.ZERO);
        }
        
        return dto;
    }
    
    @Override
    public void deleteById(Long id) {
        removeById(id);
    }
}
