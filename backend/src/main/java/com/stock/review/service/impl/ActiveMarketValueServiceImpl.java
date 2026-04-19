package com.stock.review.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.review.entity.ActiveMarketValue;
import com.stock.review.entity.dto.ActiveMarketValueDTO;
import com.stock.review.mapper.ActiveMarketValueMapper;
import com.stock.review.service.ActiveMarketValueService;
import com.stock.review.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActiveMarketValueServiceImpl extends ServiceImpl<ActiveMarketValueMapper, ActiveMarketValue> implements ActiveMarketValueService {
    
    private final SystemConfigService systemConfigService;
    
    @Override
    public ActiveMarketValue saveOrUpdate(ActiveMarketValueDTO dto) {
        ActiveMarketValue entity = new ActiveMarketValue();
        if (dto.getId() != null) {
            entity = getById(dto.getId());
        } else if (dto.getRecordDate() != null) {
            ActiveMarketValue existingRecord = getOne(new LambdaQueryWrapper<ActiveMarketValue>()
                    .eq(ActiveMarketValue::getRecordDate, dto.getRecordDate()));
            if (existingRecord != null) {
                entity = existingRecord;
            }
        }
        
        BeanUtil.copyProperties(dto, entity);
        
        if (dto.getMarketValue() != null) {
            entity.setChangePercent(dto.getMarketValue());
            
            ActiveMarketValue prevRecord = getOne(new LambdaQueryWrapper<ActiveMarketValue>()
                    .lt(ActiveMarketValue::getRecordDate, dto.getRecordDate())
                    .orderByDesc(ActiveMarketValue::getRecordDate)
                    .orderByDesc(ActiveMarketValue::getUpdateTime)
                    .orderByDesc(ActiveMarketValue::getId)
                    .last("LIMIT 1"));
            
            BigDecimal prevChangePercent = prevRecord != null ? prevRecord.getChangePercent() : null;
            String prevMarketStatus = prevRecord != null ? prevRecord.getMarketStatus() : null;
            String marketStatus = judgeMarketStatus(entity.getChangePercent(), prevChangePercent, prevMarketStatus);
            entity.setMarketStatus(marketStatus);
            entity.setOperationTip(getOperationTip(marketStatus));
        }
        
        saveOrUpdate(entity);
        return entity;
    }
    
    @Override
    public Page<ActiveMarketValue> pageList(Integer pageNum, Integer pageSize) {
        return page(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<ActiveMarketValue>()
                .orderByDesc(ActiveMarketValue::getRecordDate));
    }
    
    @Override
    public ActiveMarketValue getLatest() {
        return getOne(new LambdaQueryWrapper<ActiveMarketValue>()
                .orderByDesc(ActiveMarketValue::getRecordDate)
                .orderByDesc(ActiveMarketValue::getUpdateTime)
                .orderByDesc(ActiveMarketValue::getId)
                .last("LIMIT 1"));
    }
    
    @Override
    public List<ActiveMarketValue> getRecentList(Integer days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        return list(new LambdaQueryWrapper<ActiveMarketValue>()
                .ge(ActiveMarketValue::getRecordDate, startDate)
                .orderByDesc(ActiveMarketValue::getRecordDate));
    }
    
    @Override
    public String judgeMarketStatus(BigDecimal changePercent, BigDecimal prevChangePercent, String currentStatus) {
        if (changePercent == null) {
            if (currentStatus != null && !"震荡".equals(currentStatus)) {
                return currentStatus;
            }
            return "资金流出";
        }
        
        BigDecimal outflowThreshold = systemConfigService.getOutflowThreshold();
        BigDecimal inflowThreshold = systemConfigService.getInflowThreshold();
        
        if (changePercent.compareTo(outflowThreshold) <= 0) {
            return "资金流出";
        }
        
        if (changePercent.compareTo(inflowThreshold) >= 0) {
            return "资金流入";
        }
        
        if (prevChangePercent != null) {
            BigDecimal twoDaySum = changePercent.add(prevChangePercent);
            if (twoDaySum.compareTo(inflowThreshold) >= 0) {
                return "资金流入";
            }
        }
        
        if (currentStatus != null && !"震荡".equals(currentStatus)) {
            return currentStatus;
        }
        
        return "资金流出";
    }
    
    @Override
    public String getOperationTip(String marketStatus) {
        if ("资金流出".equals(marketStatus)) {
            return "操作放紧";
        } else if ("资金流入".equals(marketStatus)) {
            return "操作放松";
        } else {
            return "操作放紧";
        }
    }
    
    @Override
    public void recalculateAllStatus() {
        List<ActiveMarketValue> allRecords = list(new LambdaQueryWrapper<ActiveMarketValue>()
                .orderByAsc(ActiveMarketValue::getRecordDate)
                .orderByAsc(ActiveMarketValue::getId));
        
        BigDecimal prevChangePercent = null;
        String prevMarketStatus = null;
        
        for (ActiveMarketValue record : allRecords) {
            if (record.getMarketValue() != null) {
                record.setChangePercent(record.getMarketValue());
                String marketStatus = judgeMarketStatus(record.getChangePercent(), prevChangePercent, prevMarketStatus);
                record.setMarketStatus(marketStatus);
                record.setOperationTip(getOperationTip(marketStatus));
            }
            prevChangePercent = record.getChangePercent();
            prevMarketStatus = record.getMarketStatus();
        }
        
        updateBatchById(allRecords);
    }
}
