package com.stock.review.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.review.common.Result;
import com.stock.review.entity.StockSelection;
import com.stock.review.entity.dto.StockSelectionDTO;
import com.stock.review.service.StockSelectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/stock-selection")
@RequiredArgsConstructor
public class StockSelectionController {
    
    private final StockSelectionService stockSelectionService;
    
    @PostMapping("/save")
    public Result<StockSelection> save(@Valid @RequestBody StockSelectionDTO dto) {
        return Result.success(stockSelectionService.saveOrUpdate(dto));
    }
    
    @GetMapping("/page")
    public Result<Page<StockSelection>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(stockSelectionService.pageList(pageNum, pageSize));
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        stockSelectionService.deleteById(id);
        return Result.success();
    }
    
    @PostMapping("/mark-operated/{id}")
    public Result<Void> markAsOperated(@PathVariable Long id) {
        stockSelectionService.markAsOperated(id);
        return Result.success();
    }
    
    @PostMapping("/refresh-prices")
    public Result<Void> refreshPrices() {
        stockSelectionService.refreshPrices();
        return Result.success();
    }
}
