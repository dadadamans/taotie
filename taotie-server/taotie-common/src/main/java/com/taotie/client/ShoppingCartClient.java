package com.taotie.client;

import com.taotie.client.fallback.ShoppingCartClientFallbackFactory;
import com.taotie.entity.ShoppingCart;
import com.taotie.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@FeignClient(name = "taotie-order", fallbackFactory = ShoppingCartClientFallbackFactory.class)
public interface ShoppingCartClient {

    @GetMapping("/user/shoppingCart/list")
    Result<List<ShoppingCart>> list();

    @DeleteMapping("/user/shoppingCart/clean")
    Result<String> clean();
}