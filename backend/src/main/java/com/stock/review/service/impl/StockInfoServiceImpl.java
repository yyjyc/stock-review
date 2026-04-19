package com.stock.review.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.review.dto.PreloadStatus;
import com.stock.review.entity.StockInfo;
import com.stock.review.mapper.StockInfoMapper;
import com.stock.review.service.StockInfoService;
import com.stock.review.utils.StockApiUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockInfoServiceImpl extends ServiceImpl<StockInfoMapper, StockInfo> implements StockInfoService {
    
    private final StockApiUtil stockApiUtil;
    
    private final AtomicReference<PreloadStatus> preloadStatus = new AtomicReference<>(PreloadStatus.idle());
    
    @Override
    public List<StockInfo> search(String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return Collections.emptyList();
        }
        
        final String searchKeyword = keyword.toUpperCase();
        
        return list(new LambdaQueryWrapper<StockInfo>()
                .and(wrapper -> wrapper
                        .like(StockInfo::getStockName, searchKeyword)
                        .or()
                        .like(StockInfo::getStockCode, searchKeyword)
                        .or()
                        .like(StockInfo::getPinyin, searchKeyword)
                        .or()
                        .like(StockInfo::getFirstLetter, searchKeyword)
                )
                .last("LIMIT 20"));
    }
    
    @Override
    public StockInfo getByCode(String stockCode) {
        return getOne(new LambdaQueryWrapper<StockInfo>()
                .eq(StockInfo::getStockCode, stockCode));
    }
    
    @Override
    public PreloadStatus getPreloadStatus() {
        return preloadStatus.get();
    }
    
    @Override
    @Async
    public void preloadStockInfoAsync() {
        PreloadStatus currentStatus = preloadStatus.get();
        if (currentStatus.isLoading()) {
            log.info("预加载正在进行中，请勿重复操作");
            return;
        }
        
        log.info("开始预加载股票信息...");
        preloadStatus.set(PreloadStatus.loading(0, 0));
        
        try {
            List<Map<String, Object>> stockList = stockApiUtil.getAllStockList();
            if (stockList.isEmpty()) {
                log.warn("获取股票列表为空");
                preloadStatus.set(PreloadStatus.failed("获取股票列表为空"));
                return;
            }
            
            int total = stockList.size();
            log.info("获取到 {} 条股票信息", total);
            
            AtomicInteger count = new AtomicInteger(0);
            int batchSize = 100;
            
            for (int i = 0; i < stockList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, stockList.size());
                List<Map<String, Object>> batch = stockList.subList(i, end);
                
                processBatch(batch);
                count.addAndGet(batch.size());
                
                preloadStatus.set(PreloadStatus.loading(total, count.get()));
                
                if (count.get() % 500 == 0) {
                    log.info("已处理 {}/{} 条股票信息", count.get(), total);
                }
            }
            
            preloadStatus.set(PreloadStatus.completed(count.get()));
            log.info("预加载完成，共处理 {} 条股票信息", count.get());
            
        } catch (Exception e) {
            log.error("预加载股票信息失败", e);
            preloadStatus.set(PreloadStatus.failed(e.getMessage()));
        }
    }
    
    @Transactional
    public void processBatch(List<Map<String, Object>> batch) {
        for (Map<String, Object> stock : batch) {
            String stockCode = (String) stock.get("stockCode");
            String stockName = (String) stock.get("stockName");
            
            if (StrUtil.isBlank(stockCode) || StrUtil.isBlank(stockName)) {
                continue;
            }
            
            StockInfo existStock = getByCode(stockCode);
            if (existStock != null) {
                existStock.setStockName(stockName);
                existStock.setPinyin((String) stock.get("pinyin"));
                existStock.setFirstLetter((String) stock.get("firstLetter"));
                existStock.setMarket((String) stock.get("market"));
                updateById(existStock);
            } else {
                StockInfo newStock = new StockInfo();
                newStock.setStockCode(stockCode);
                newStock.setStockName(stockName);
                newStock.setPinyin((String) stock.get("pinyin"));
                newStock.setFirstLetter((String) stock.get("firstLetter"));
                newStock.setMarket((String) stock.get("market"));
                save(newStock);
            }
        }
    }
}
