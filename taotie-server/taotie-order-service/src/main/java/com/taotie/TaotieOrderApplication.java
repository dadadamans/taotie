package com.taotie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@Slf4j
@EnableScheduling
@EnableDiscoveryClient
@EnableTransactionManagement
@EnableRabbit
@SpringBootApplication(scanBasePackages = {"com.taotie"})
@EnableFeignClients(basePackages = "com.taotie.client")
public class TaotieOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaotieOrderApplication.class, args);
        log.info("taotie-order 订单微服务启动成功！");
    }
}
