package com.stock.review.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class StockExperienceDTO {
    private Long id;
    
    @NotBlank(message = "经验标题不能为空")
    private String title;
    
    @NotBlank(message = "经验内容不能为空")
    private String content;
}
