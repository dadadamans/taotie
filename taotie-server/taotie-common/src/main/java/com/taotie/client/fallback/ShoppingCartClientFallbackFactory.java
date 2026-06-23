package com.taotie.client.fallback;

import com.taotie.client.ShoppingCartClient;
import com.taotie.entity.ShoppingCart;
import com.taotie.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class ShoppingCartClientFallbackFactory implements FallbackFactory<ShoppingCartClient> {

    @Override
    public ShoppingCartClient create(Throwable cause) {
        log.error("ShoppingCartClient 触发熔断，异常原因: {}", cause.getMessage());

        return new ShoppingCartClient() {

            @Override
            public Result<List<ShoppingCart>> list() {
                return Result.success(Collections.emptyList());
            }

            @Override
            public Result<String> clean() {
                return Result.error("购物车服务繁忙，请稍后再试");
            }
        };
    }
}
