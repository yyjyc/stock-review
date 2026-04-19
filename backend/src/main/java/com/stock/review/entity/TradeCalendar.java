package com.stock.review.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("trade_calendar")
public class TradeCalendar implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private LocalDate tradeDate;
    
    private Integer isTradeDay;
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
