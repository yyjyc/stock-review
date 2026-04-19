package com.stock.review.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.review.common.Result;
import com.stock.review.entity.StockExperience;
import com.stock.review.entity.dto.StockExperienceDTO;
import com.stock.review.service.StockExperienceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/stock-experience")
@RequiredArgsConstructor
public class StockExperienceController {
    
    private final StockExperienceService stockExperienceService;
    
    @PostMapping("/save")
    public Result<StockExperience> save(@Valid @RequestBody StockExperienceDTO dto) {
        return Result.success(stockExperienceService.saveOrUpdate(dto));
    }
    
    @GetMapping("/page")
    public Result<Page<StockExperience>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(stockExperienceService.pageList(pageNum, pageSize));
    }
    
    @GetMapping("/list")
    public Result<List<StockExperience>> list() {
        return Result.success(stockExperienceService.list());
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        stockExperienceService.deleteById(id);
        return Result.success();
    }
}
