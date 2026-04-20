package com.stock.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.review.entity.AdjustReason;
import com.stock.review.mapper.AdjustReasonMapper;
import com.stock.review.service.AdjustReasonService;
import com.stock.review.utils.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdjustReasonServiceImpl extends ServiceImpl<AdjustReasonMapper, AdjustReason> implements AdjustReasonService {
    
    @Override
    public List<AdjustReason> getAllReasons() {
        return list(new LambdaQueryWrapper<AdjustReason>()
                .eq(AdjustReason::getUserId, SecurityUtils.getCurrentUserId())
                .orderByDesc(AdjustReason::getCreateTime));
    }
    
    @Override
    public void saveReason(AdjustReason reason) {
        reason.setUserId(SecurityUtils.getCurrentUserId());
        saveOrUpdate(reason);
    }
    
    @Override
    public void deleteReason(Long id) {
        AdjustReason entity = getById(id);
        if (entity != null && !entity.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new RuntimeException("无权删除他人数据");
        }
        removeById(id);
    }
}
