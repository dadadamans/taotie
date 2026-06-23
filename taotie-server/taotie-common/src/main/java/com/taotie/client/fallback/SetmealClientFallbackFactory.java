package com.taotie.client.fallback;

import com.taotie.client.SetmealClient;
import com.taotie.entity.Setmeal;
import com.taotie.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SetmealClientFallbackFactory implements FallbackFactory<SetmealClient> {

    @Override
    public SetmealClient create(Throwable cause) {
        log.error("SetmealClient 触发熔断，异常原因: {}", cause.getMessage());

        return new SetmealClient() {

            @Override
            public Result<Setmeal> getById(Long id) {
                return Result.error("商品服务繁忙，请稍后再试");
            }

            @Override
            public Result<Integer> countByStatus(Integer status) {
                return Result.error("商品服务繁忙，请稍后再试");
            }
        };
    }
}
