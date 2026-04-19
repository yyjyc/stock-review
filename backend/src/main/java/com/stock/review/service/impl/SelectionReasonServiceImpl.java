package com.stock.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.review.entity.SelectionReason;
import com.stock.review.mapper.SelectionReasonMapper;
import com.stock.review.service.SelectionReasonService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SelectionReasonServiceImpl extends ServiceImpl<SelectionReasonMapper, SelectionReason> implements SelectionReasonService {
    
    @Override
    public List<SelectionReason> getAllReasons() {
        return list(new LambdaQueryWrapper<SelectionReason>()
                .orderByDesc(SelectionReason::getCreateTime));
    }
    
    @Override
    public void saveReason(SelectionReason reason) {
        saveOrUpdate(reason);
    }
    
    @Override
    public void deleteReason(Long id) {
        removeById(id);
    }
}
