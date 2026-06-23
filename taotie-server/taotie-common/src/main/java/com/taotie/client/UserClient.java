package com.taotie.client;

import com.taotie.client.fallback.UserClientFallbackFactory;
import com.taotie.entity.AddressBook;
import com.taotie.entity.User;
import com.taotie.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@FeignClient(name = "taotie-user", fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient {

    @GetMapping("/user/addressBook/{id}")
    Result<AddressBook> getAddressBookById(@PathVariable("id") Long id);

    @GetMapping("/user/user/{id}")
    Result<User> getById(@PathVariable("id") Long id);

    @GetMapping("/user/user/count")
    Result<Integer> countByTimeRange(
            @RequestParam(value = "begin", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime begin,
            @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end);
}