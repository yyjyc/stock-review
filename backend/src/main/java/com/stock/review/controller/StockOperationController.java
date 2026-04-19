package com.stock.review.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.review.common.Result;
import com.stock.review.entity.StockOperation;
import com.stock.review.entity.dto.OperationStatisticsDTO;
import com.stock.review.entity.dto.StockOperationDTO;
import com.stock.review.service.StockOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/stock-operation")
@RequiredArgsConstructor
public class StockOperationController {
    
    private final StockOperationService stockOperationService;
    
    @PostMapping("/save")
    public Result<StockOperation> save(@Valid @RequestBody StockOperationDTO dto) {
        return Result.success(stockOperationService.saveOrUpdate(dto));
    }
    
    @GetMapping("/page")
    public Result<Page<StockOperation>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String stockName,
            @RequestParam(required = false) Integer followRule) {
        return Result.success(stockOperationService.pageList(pageNum, pageSize, startDate, endDate, stockName, followRule));
    }
    
    @GetMapping("/statistics")
    public Result<OperationStatisticsDTO> getStatistics() {
        return Result.success(stockOperationService.getStatistics());
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        stockOperationService.deleteById(id);
        return Result.success();
    }
}
