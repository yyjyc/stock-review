package com.stock.review.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stock.review.entity.Position;
import com.stock.review.entity.dto.PositionDTO;
import com.stock.review.entity.dto.PositionSummaryDTO;

import java.util.List;

public interface PositionService extends IService<Position> {
    
    Position createPosition(String stockName, String stockCode, java.math.BigDecimal amount, java.math.BigDecimal price);
    
    Position addPosition(Long positionId, java.math.BigDecimal amount, java.math.BigDecimal price);
    
    Position reducePosition(Long positionId, java.math.BigDecimal amount, java.math.BigDecimal price);
    
    Position clearPosition(Long positionId, java.math.BigDecimal price);
    
    Page<Position> pageList(Integer pageNum, Integer pageSize, String status, String sortProp, String sortOrder);
    
    PositionSummaryDTO getSummary();
    
    void refreshPrices();
    
    Position getByStockCode(String stockCode);
    
    List<Position> getActivePositions();
    
    Position initPosition(PositionDTO dto);
    
    void savePlan(com.stock.review.controller.PositionController.PositionPlanDTO dto);
    
    Position updatePosition(PositionDTO dto);
    
    void deletePosition(Long id);
    
    void closeAlert(Long id);
}
