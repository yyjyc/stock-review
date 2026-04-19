package com.stock.review.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stock.review.entity.StockOperation;
import com.stock.review.entity.dto.OperationStatisticsDTO;
import com.stock.review.entity.dto.StockOperationDTO;

public interface StockOperationService extends IService<StockOperation> {
    
    StockOperation saveOrUpdate(StockOperationDTO dto);
    
    Page<StockOperation> pageList(Integer pageNum, Integer pageSize, String startDate, String endDate, String stockName, Integer followRule);
    
    OperationStatisticsDTO getStatistics();
    
    OperationStatisticsDTO getStatisticsByDateRange(String startDate, String endDate);
    
    void deleteById(Long id);
}
