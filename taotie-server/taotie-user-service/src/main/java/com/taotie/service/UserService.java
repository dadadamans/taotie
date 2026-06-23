package com.taotie.service;

import com.taotie.entity.User;

import java.time.LocalDateTime;

public interface UserService {

    /**
     * 根据id查询用户
     * @param id
     * @return
     */
    User getById(Long id);

    /**
     * 根据时间范围统计用户数量
     * @param begin
     * @param end
     * @return
     */
    Integer countByTimeRange(LocalDateTime begin, LocalDateTime end);
}
