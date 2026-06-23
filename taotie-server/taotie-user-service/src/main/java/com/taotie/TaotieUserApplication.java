package com.taotie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@Slf4j
@EnableDiscoveryClient // 开启服务发现，将当前服务注册到 Nacos
@SpringBootApplication(scanBasePackages = {"com.taotie"}) // 👈 核心：加上这个，确保扫描到公共模块里的切面和异常处理器
public class TaotieUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaotieUserApplication.class, args);
        log.info("taotie-user 用户微服务启动成功！");
    }
}