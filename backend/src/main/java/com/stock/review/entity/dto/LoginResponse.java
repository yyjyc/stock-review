package com.stock.review.entity.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private String token;
    private UserInfoDTO userInfo;

    public LoginResponse(String token, UserInfoDTO userInfo) {
        this.token = token;
        this.userInfo = userInfo;
    }
}
