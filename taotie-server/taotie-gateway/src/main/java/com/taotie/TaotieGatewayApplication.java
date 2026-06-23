package com.taotie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
// 🚀 精准组件扫描控制
@ComponentScan(
        basePackages = {"com.taotie"},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASPECTJ,
                        // 💡 排除所有普通微服务用的拦截器（里面包含 javax.servlet，网关不认识）
                        pattern = "com.taotie.interceptor.*"
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASPECTJ,
                        // 💡 排除 common 里普通的 WebMvc 配置类（通常也会引入 servlet 导致报错）
                        pattern = "com.taotie.config.*"
                )
        }
)
public class TaotieGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaotieGatewayApplication.class, args);
    }
}