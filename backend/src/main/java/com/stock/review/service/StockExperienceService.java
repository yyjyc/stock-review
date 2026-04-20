package com.stock.review.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.stock.review.entity.StockExperience;
import com.stock.review.entity.dto.StockExperienceDTO;

import java.util.List;

public interface StockExperienceService extends IService<StockExperience> {
    
    StockExperience saveOrUpdate(StockExperienceDTO dto);
    
    Page<StockExperience> pageList(Integer pageNum, Integer pageSize);
    
    void deleteById(Long id);
    
    List<StockExperience> getUserList();
}
