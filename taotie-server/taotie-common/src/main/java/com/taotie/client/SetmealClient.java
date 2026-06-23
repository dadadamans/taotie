package com.taotie.client;

import com.taotie.client.fallback.SetmealClientFallbackFactory;
import com.taotie.entity.Setmeal;
import com.taotie.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "taotie-shop", contextId = "setmealClient", fallbackFactory = SetmealClientFallbackFactory.class)
public interface SetmealClient {

    @GetMapping("/user/setmeal/{id}")
    Result<Setmeal> getById(@PathVariable("id") Long id);

    @GetMapping("/admin/setmeal/countByStatus")
    Result<Integer> countByStatus(@RequestParam("status") Integer status);
}
