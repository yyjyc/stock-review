package com.stock.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.review.entity.SelectionReason;
import com.stock.review.mapper.SelectionReasonMapper;
import com.stock.review.service.SelectionReasonService;
import com.stock.review.utils.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SelectionReasonServiceImpl extends ServiceImpl<SelectionReasonMapper, SelectionReason> implements SelectionReasonService {
    
    @Override
    public List<SelectionReason> getAllReasons() {
        return list(new LambdaQueryWrapper<SelectionReason>()
                .eq(SelectionReason::getUserId, SecurityUtils.getCurrentUserId())
                .orderByDesc(SelectionReason::getCreateTime));
    }
    
    @Override
    public void saveReason(SelectionReason reason) {
        reason.setUserId(SecurityUtils.getCurrentUserId());
        saveOrUpdate(reason);
    }
    
    @Override
    public void deleteReason(Long id) {
        SelectionReason entity = getById(id);
        if (entity != null && !entity.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new RuntimeException("无权删除他人数据");
        }
        removeById(id);
    }
}
