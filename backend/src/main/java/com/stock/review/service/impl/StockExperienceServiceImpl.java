package com.stock.review.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.review.entity.StockExperience;
import com.stock.review.entity.dto.StockExperienceDTO;
import com.stock.review.mapper.StockExperienceMapper;
import com.stock.review.service.StockExperienceService;
import com.stock.review.utils.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockExperienceServiceImpl extends ServiceImpl<StockExperienceMapper, StockExperience> implements StockExperienceService {
    
    @Override
    public StockExperience saveOrUpdate(StockExperienceDTO dto) {
        StockExperience entity = new StockExperience();
        if (dto.getId() != null) {
            entity = getById(dto.getId());
        }
        
        BeanUtil.copyProperties(dto, entity);
        entity.setUserId(SecurityUtils.getCurrentUserId());
        saveOrUpdate(entity);
        return entity;
    }
    
    @Override
    public Page<StockExperience> pageList(Integer pageNum, Integer pageSize) {
        return page(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<StockExperience>()
                .eq(StockExperience::getUserId, SecurityUtils.getCurrentUserId())
                .orderByDesc(StockExperience::getCreateTime));
    }
    
    @Override
    public void deleteById(Long id) {
        StockExperience entity = getById(id);
        if (entity != null && !entity.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new RuntimeException("无权删除他人数据");
        }
        removeById(id);
    }
    
    @Override
    public List<StockExperience> getUserList() {
        return list(new LambdaQueryWrapper<StockExperience>()
                .eq(StockExperience::getUserId, SecurityUtils.getCurrentUserId())
                .orderByDesc(StockExperience::getCreateTime));
    }
}
