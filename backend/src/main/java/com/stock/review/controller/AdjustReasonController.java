package com.stock.review.controller;

import com.stock.review.common.Result;
import com.stock.review.entity.AdjustReason;
import com.stock.review.service.AdjustReasonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/adjust-reason")
@RequiredArgsConstructor
public class AdjustReasonController {
    
    private final AdjustReasonService adjustReasonService;
    
    @GetMapping("/list")
    public Result<List<AdjustReason>> list() {
        return Result.success(adjustReasonService.getAllReasons());
    }
    
    @PostMapping("/save")
    public Result<Void> save(@RequestBody AdjustReason reason) {
        adjustReasonService.saveReason(reason);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adjustReasonService.deleteReason(id);
        return Result.success();
    }
}
