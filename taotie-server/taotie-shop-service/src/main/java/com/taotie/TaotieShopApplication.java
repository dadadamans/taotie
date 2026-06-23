package com.taotie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@EnableDiscoveryClient     // 1. 开启服务发现，让网关能通过 Nacos 找到 shop-service
@EnableTransactionManagement // 2. 开启声明式事务
@EnableCaching             // 3. 开启 Spring Cache（@Cacheable/@CacheEvict 生效）
@SpringBootApplication(scanBasePackages = {"com.taotie"}) // 4. 核心：扫描公共模块
public class TaotieShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaotieShopApplication.class, args);
        log.info("taotie-shop 商品/店铺微服务启动成功！");
    }
}