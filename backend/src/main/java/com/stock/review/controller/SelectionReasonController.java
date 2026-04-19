package com.stock.review.controller;

import com.stock.review.common.Result;
import com.stock.review.entity.SelectionReason;
import com.stock.review.service.SelectionReasonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/selection-reason")
@RequiredArgsConstructor
public class SelectionReasonController {
    
    private final SelectionReasonService selectionReasonService;
    
    @GetMapping("/list")
    public Result<List<SelectionReason>> list() {
        return Result.success(selectionReasonService.getAllReasons());
    }
    
    @PostMapping("/save")
    public Result<Void> save(@RequestBody SelectionReason reason) {
        selectionReasonService.saveReason(reason);
        return Result.success();
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        selectionReasonService.deleteReason(id);
        return Result.success();
    }
}
