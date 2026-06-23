package com.taotie.service.impl;

import com.taotie.client.DishClient;
import com.taotie.client.UserClient;
import com.taotie.client.SetmealClient;
import com.taotie.result.Result;
import com.taotie.constant.StatusConstant;
import com.taotie.entity.Orders;
import com.taotie.mapper.OrderMapper;
import com.taotie.service.WorkspaceService;
import com.taotie.vo.BusinessDataVO;
import com.taotie.vo.DishOverViewVO;
import com.taotie.vo.OrderOverViewVO;
import com.taotie.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserClient userClient;
    @Autowired
    private DishClient dishClient;
    @Autowired
    private SetmealClient setmealClient;

    /**
     * 根据时间段统计营业数据
     * @param begin
     * @param end
     * @return
     */
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        Map map = new HashMap();
        map.put("begin",begin);
        map.put("end",end);

        Integer totalOrderCount = orderMapper.countByMap(map);

        map.put("status", Orders.COMPLETED);
        Double turnover = orderMapper.sumByMap(map);
        turnover = turnover == null? 0.0 : turnover;

        Integer validOrderCount = orderMapper.countByMap(map);

        Double unitPrice = 0.0;

        Double orderCompletionRate = 0.0;
        if(totalOrderCount != 0 && validOrderCount != 0){
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
            unitPrice = turnover / validOrderCount;
        }

        Integer newUsers = 0;
        try {
            Result<Integer> result = userClient.countByTimeRange(begin, end);
            if (result != null && result.getData() != null) {
                newUsers = result.getData();
            }
        } catch (Exception e) {
            log.error("远程调用 taotie-user 获取用户统计失败", e);
        }

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }


    /**
     * 查询订单管理数据
     *
     * @return
     */
    public OrderOverViewVO getOrderOverView() {
        Map map = new HashMap();
        map.put("begin", LocalDateTime.now().with(LocalTime.MIN));
        map.put("status", Orders.TO_BE_CONFIRMED);

        Integer waitingOrders = orderMapper.countByMap(map);

        map.put("status", Orders.CONFIRMED);
        Integer deliveredOrders = orderMapper.countByMap(map);

        map.put("status", Orders.COMPLETED);
        Integer completedOrders = orderMapper.countByMap(map);

        map.put("status", Orders.CANCELLED);
        Integer cancelledOrders = orderMapper.countByMap(map);

        map.put("status", null);
        Integer allOrders = orderMapper.countByMap(map);

        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

    public DishOverViewVO getDishOverView() {
        Integer sold = 0, discontinued = 0;
        try {
            Result<Integer> result = dishClient.countByStatus(StatusConstant.ENABLE);
            if (result != null && result.getData() != null) sold = result.getData();
            result = dishClient.countByStatus(StatusConstant.DISABLE);
            if (result != null && result.getData() != null) discontinued = result.getData();
        } catch (Exception e) {
            log.error("远程调用 taotie-shop 获取菜品概览失败", e);
        }

        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    public SetmealOverViewVO getSetmealOverView() {
        Integer sold = 0, discontinued = 0;
        try {
            Result<Integer> result = setmealClient.countByStatus(StatusConstant.ENABLE);
            if (result != null && result.getData() != null) sold = result.getData();
            result = setmealClient.countByStatus(StatusConstant.DISABLE);
            if (result != null && result.getData() != null) discontinued = result.getData();
        } catch (Exception e) {
            log.error("远程调用 taotie-shop 获取套餐概览失败", e);
        }

        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}
