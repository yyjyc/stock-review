package com.stock.review.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stock.review.entity.StockSelection;
import com.stock.review.entity.dto.StockSelectionDTO;

public interface StockSelectionService extends IService<StockSelection> {
    
    StockSelection saveOrUpdate(StockSelectionDTO dto);
    
    Page<StockSelection> pageList(Integer pageNum, Integer pageSize);
    
    void deleteById(Long id);
    
    void markAsOperated(Long id);
    
    void refreshPrices();
}
