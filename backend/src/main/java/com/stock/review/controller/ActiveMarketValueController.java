package com.stock.review.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.review.common.Result;
import com.stock.review.entity.ActiveMarketValue;
import com.stock.review.entity.dto.ActiveMarketValueDTO;
import com.stock.review.service.ActiveMarketValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/active-market-value")
@RequiredArgsConstructor
public class ActiveMarketValueController {
    
    private final ActiveMarketValueService activeMarketValueService;
    
    @PostMapping("/save")
    public Result<ActiveMarketValue> save(@Valid @RequestBody ActiveMarketValueDTO dto) {
        return Result.success(activeMarketValueService.saveOrUpdate(dto));
    }
    
    @GetMapping("/page")
    public Result<Page<ActiveMarketValue>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(activeMarketValueService.pageList(pageNum, pageSize));
    }
    
    @GetMapping("/latest")
    public Result<ActiveMarketValue> getLatest() {
        return Result.success(activeMarketValueService.getLatest());
    }
    
    @GetMapping("/recent")
    public Result<List<ActiveMarketValue>> getRecent(@RequestParam(defaultValue = "30") Integer days) {
        return Result.success(activeMarketValueService.getRecentList(days));
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        activeMarketValueService.removeById(id);
        return Result.success();
    }
    
    @PostMapping("/recalculate")
    public Result<Void> recalculate() {
        activeMarketValueService.recalculateAllStatus();
        return Result.success();
    }
}
