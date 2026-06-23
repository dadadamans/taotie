package com.taotie.service.impl;

import com.taotie.entity.User;
import com.taotie.mapper.UserMapper;
import com.taotie.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 根据id查询用户
     * @param id
     * @return
     */
    public User getById(Long id) {
        return userMapper.getById(id);
    }

    /**
     * 根据时间范围统计用户数量
     * @param begin
     * @param end
     * @return
     */
    public Integer countByTimeRange(LocalDateTime begin, LocalDateTime end) {
        Map map = new HashMap();
        if (begin != null) {
            map.put("begin", begin);
        }
        map.put("end", end);
        return userMapper.countByMap(map);
    }
}
