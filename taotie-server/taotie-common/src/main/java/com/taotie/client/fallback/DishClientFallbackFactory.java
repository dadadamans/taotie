package com.taotie.client.fallback;

import com.taotie.client.DishClient;
import com.taotie.entity.Dish;
import com.taotie.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DishClientFallbackFactory implements FallbackFactory<DishClient> {

    @Override
    public DishClient create(Throwable cause) {
        log.error("DishClient 触发熔断，异常原因: {}", cause.getMessage());

        return new DishClient() {

            @Override
            public Result<Dish> getById(Long id) {
                return Result.error("商品服务繁忙，请稍后再试");
            }

            @Override
            public Result<Integer> countByStatus(Integer status) {
                return Result.error("商品服务繁忙，请稍后再试");
            }
        };
    }
}
