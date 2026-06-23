package com.taotie.client.fallback;

import com.taotie.client.UserClient;
import com.taotie.entity.AddressBook;
import com.taotie.entity.User;
import com.taotie.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        log.error("UserClient 触发熔断，异常原因: {}", cause.getMessage());

        return new UserClient() {

            @Override
            public Result<AddressBook> getAddressBookById(Long id) {
                return Result.error("用户服务繁忙，请稍后再试");
            }

            @Override
            public Result<User> getById(Long id) {
                return Result.error("用户服务繁忙，请稍后再试");
            }

            @Override
            public Result<Integer> countByTimeRange(LocalDateTime begin, LocalDateTime end) {
                return Result.error("用户服务繁忙，请稍后再试");
            }
        };
    }
}
