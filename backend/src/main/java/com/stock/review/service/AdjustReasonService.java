package com.stock.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stock.review.entity.AdjustReason;

import java.util.List;

public interface AdjustReasonService extends IService<AdjustReason> {
    
    List<AdjustReason> getAllReasons();
    
    void saveReason(AdjustReason reason);
    
    void deleteReason(Long id);
}
