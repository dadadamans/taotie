package com.taotie.service;

import com.taotie.dto.UserLoginDTO;
import com.taotie.entity.User;

public interface UserService {
    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    User wxLogin(UserLoginDTO userLoginDTO);
}
