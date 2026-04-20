package com.stock.review.entity.dto;

import lombok.Data;

@Data
public class UserInfoDTO {

    private Long id;
    private String username;
    private String nickname;
    private String role;
    private Integer status;

    public UserInfoDTO() {}

    public UserInfoDTO(Long id, String username, String nickname, String role, Integer status) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.role = role;
        this.status = status;
    }
}
