package com.taotie.config;

import com.taotie.interceptor.GatewayHeaderInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

@Configuration
@Slf4j
public class WebMvcConfiguration extends BaseWebMvcConfiguration {

    // 🌟 注入全新升级的、身兼三职（验暗号、拿ID、清线程）的网关拦截器
    @Autowired
    private GatewayHeaderInterceptor gatewayHeaderInterceptor;

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("🛡️ 开始为当前微服务拉起安全防御并注册网关拦截器...");

        // 拦截所有请求，确保无论是 /admin 还是 /user 都能被保护到
        registry.addInterceptor(gatewayHeaderInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/admin/employee/login", // 登录接口可以放行（因为网关鉴权也会放行它）
                        "/user/user/login",      // 登录接口放行
                        "/doc.html",             // 放行 Swagger 生成的接口文档
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v2/api-docs"
                );
    }
}
