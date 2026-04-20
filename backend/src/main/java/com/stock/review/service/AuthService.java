package com.stock.review.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stock.review.entity.User;
import com.stock.review.entity.dto.*;
import com.stock.review.mapper.UserMapper;
import com.stock.review.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        log.info("登录尝试 - 用户名: {}", request.getUsername());
        
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );

        if (user == null) {
            log.warn("用户不存在: {}", request.getUsername());
            throw new RuntimeException("用户名或密码错误");
        }
        
        log.info("找到用户: {}, 密码hash: {}", user.getUsername(), user.getPassword());
        log.info("输入密码: {}", request.getPassword());
        
        boolean passwordMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());
        log.info("密码匹配结果: {}", passwordMatch);
        
        if (!passwordMatch) {
            log.warn("密码不匹配");
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());
        UserInfoDTO userInfo = toUserInfoDTO(user);

        log.info("登录成功: {}", user.getUsername());
        return new LoginResponse(token, userInfo);
    }

    public UserInfoDTO register(RegisterRequest request) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);

        return toUserInfoDTO(user);
    }

    public UserInfoDTO getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("未登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return toUserInfoDTO(user);
    }

    public void changePassword(ChangePasswordRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    private UserInfoDTO toUserInfoDTO(User user) {
        return new UserInfoDTO(user.getId(), user.getUsername(), user.getNickname(), user.getRole(), user.getStatus());
    }
}
