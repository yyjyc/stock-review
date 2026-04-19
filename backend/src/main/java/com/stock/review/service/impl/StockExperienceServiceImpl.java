package com.stock.review.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.review.entity.StockExperience;
import com.stock.review.entity.dto.StockExperienceDTO;
import com.stock.review.mapper.StockExperienceMapper;
import com.stock.review.service.StockExperienceService;
import org.springframework.stereotype.Service;

@Service
public class StockExperienceServiceImpl extends ServiceImpl<StockExperienceMapper, StockExperience> implements StockExperienceService {
    
    @Override
    public StockExperience saveOrUpdate(StockExperienceDTO dto) {
        StockExperience entity = new StockExperience();
        if (dto.getId() != null) {
            entity = getById(dto.getId());
        }
        
        BeanUtil.copyProperties(dto, entity);
        saveOrUpdate(entity);
        return entity;
    }
    
    @Override
    public Page<StockExperience> pageList(Integer pageNum, Integer pageSize) {
        return page(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<StockExperience>()
                .orderByDesc(StockExperience::getCreateTime));
    }
    
    @Override
    public void deleteById(Long id) {
        removeById(id);
    }
}
