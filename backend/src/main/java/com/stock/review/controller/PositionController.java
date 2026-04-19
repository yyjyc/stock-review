package com.stock.review.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.review.common.Result;
import com.stock.review.entity.Position;
import com.stock.review.entity.dto.PositionDTO;
import com.stock.review.entity.dto.PositionSummaryDTO;
import com.stock.review.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/position")
@RequiredArgsConstructor
public class PositionController {
    
    private final PositionService positionService;
    
    @GetMapping("/page")
    public Result<Page<Position>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortProp,
            @RequestParam(required = false) String sortOrder) {
        return Result.success(positionService.pageList(pageNum, pageSize, status, sortProp, sortOrder));
    }
    
    @GetMapping("/summary")
    public Result<PositionSummaryDTO> getSummary() {
        return Result.success(positionService.getSummary());
    }
    
    @PostMapping("/refresh-prices")
    public Result<Void> refreshPrices() {
        positionService.refreshPrices();
        return Result.success();
    }
    
    @GetMapping("/{id}")
    public Result<Position> getById(@PathVariable Long id) {
        return Result.success(positionService.getById(id));
    }
    
    @PostMapping("/init")
    public Result<Position> initPosition(@RequestBody PositionDTO dto) {
        return Result.success(positionService.initPosition(dto));
    }
    
    @PostMapping("/add")
    public Result<Position> addPosition(@RequestBody PositionAdjustDTO dto) {
        return Result.success(positionService.addPosition(dto.getPositionId(), dto.getAmount(), dto.getPrice()));
    }
    
    @PostMapping("/reduce")
    public Result<Position> reducePosition(@RequestBody PositionAdjustDTO dto) {
        return Result.success(positionService.reducePosition(dto.getPositionId(), dto.getAmount(), dto.getPrice()));
    }
    
    @PostMapping("/clear")
    public Result<Position> clearPosition(@RequestBody PositionClearDTO dto) {
        return Result.success(positionService.clearPosition(dto.getPositionId(), dto.getPrice()));
    }
    
    @PostMapping("/plan")
    public Result<Void> savePlan(@RequestBody PositionPlanDTO dto) {
        positionService.savePlan(dto);
        return Result.success();
    }
    
    @PutMapping("/{id}")
    public Result<Position> updatePosition(@PathVariable Long id, @RequestBody PositionDTO dto) {
        dto.setId(id);
        return Result.success(positionService.updatePosition(dto));
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deletePosition(@PathVariable Long id) {
        positionService.deletePosition(id);
        return Result.success();
    }
    
    @PutMapping("/{id}/alert")
    public Result<Void> closeAlert(@PathVariable Long id) {
        positionService.closeAlert(id);
        return Result.success();
    }
    
    public static class PositionAdjustDTO {
        private Long positionId;
        private BigDecimal amount;
        private BigDecimal price;
        private Integer shares;
        private String reason;
        
        public Long getPositionId() { return positionId; }
        public void setPositionId(Long positionId) { this.positionId = positionId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public Integer getShares() { return shares; }
        public void setShares(Integer shares) { this.shares = shares; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    public static class PositionClearDTO {
        private Long positionId;
        private BigDecimal price;
        
        public Long getPositionId() { return positionId; }
        public void setPositionId(Long positionId) { this.positionId = positionId; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }
    
    public static class PositionPlanDTO {
        private Long id;
        private String planType;
        private Integer planShares;
        private BigDecimal planAmount;
        private String planReason;
        private String planDate;
        private String executeTime;
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getPlanType() { return planType; }
        public void setPlanType(String planType) { this.planType = planType; }
        public Integer getPlanShares() { return planShares; }
        public void setPlanShares(Integer planShares) { this.planShares = planShares; }
        public BigDecimal getPlanAmount() { return planAmount; }
        public void setPlanAmount(BigDecimal planAmount) { this.planAmount = planAmount; }
        public String getPlanReason() { return planReason; }
        public void setPlanReason(String planReason) { this.planReason = planReason; }
        public String getPlanDate() { return planDate; }
        public void setPlanDate(String planDate) { this.planDate = planDate; }
        public String getExecuteTime() { return executeTime; }
        public void setExecuteTime(String executeTime) { this.executeTime = executeTime; }
    }
}
