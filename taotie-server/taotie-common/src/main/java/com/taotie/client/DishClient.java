package com.taotie.client;

import com.taotie.client.fallback.DishClientFallbackFactory;
import com.taotie.entity.Dish;
import com.taotie.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "taotie-shop", contextId = "dishClient", fallbackFactory = DishClientFallbackFactory.class)
public interface DishClient {

    @GetMapping("/user/dish/{id}")
    Result<Dish> getById(@PathVariable("id") Long id);

    @GetMapping("/admin/dish/countByStatus")
    Result<Integer> countByStatus(@RequestParam("status") Integer status);
}
