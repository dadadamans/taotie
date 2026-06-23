package com.taotie;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableDiscoveryClient // 让 Nacos 能够发现这个新服务
@EnableTransactionManagement // 开启声明式事务支持
public class TaotieAuthApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(TaotieAuthApplication.class, args);
        System.out.println("====== 🚀 饕餮外卖-用户/员工微服务启动成功！ ======");
    }
}