package com.stock.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stock.review.entity.SelectionReason;

import java.util.List;

public interface SelectionReasonService extends IService<SelectionReason> {
    
    List<SelectionReason> getAllReasons();
    
    void saveReason(SelectionReason reason);
    
    void deleteReason(Long id);
}
