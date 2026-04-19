package com.stock.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.review.entity.AdjustReason;
import com.stock.review.mapper.AdjustReasonMapper;
import com.stock.review.service.AdjustReasonService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdjustReasonServiceImpl extends ServiceImpl<AdjustReasonMapper, AdjustReason> implements AdjustReasonService {
    
    @Override
    public List<AdjustReason> getAllReasons() {
        return list(new LambdaQueryWrapper<AdjustReason>()
                .orderByDesc(AdjustReason::getCreateTime));
    }
    
    @Override
    public void saveReason(AdjustReason reason) {
        saveOrUpdate(reason);
    }
    
    @Override
    public void deleteReason(Long id) {
        removeById(id);
    }
}
