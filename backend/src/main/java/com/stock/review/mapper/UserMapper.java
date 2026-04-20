package com.stock.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.review.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
