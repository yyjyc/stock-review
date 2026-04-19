package com.stock.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stock.review.dto.PreloadStatus;
import com.stock.review.entity.StockInfo;

import java.util.List;

public interface StockInfoService extends IService<StockInfo> {
    
    List<StockInfo> search(String keyword);
    
    StockInfo getByCode(String stockCode);
    
    void preloadStockInfoAsync();
    
    PreloadStatus getPreloadStatus();
}
