package com.taotie.config;

import com.taotie.interceptor.GatewayHeaderInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

@Configuration
public class WebMvcConfiguration extends BaseWebMvcConfiguration {

    @Autowired
    private GatewayHeaderInterceptor gatewayHeaderInterceptor;

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        // 拦截所有请求，全面接管安全校验与用户上下文提取
        registry.addInterceptor(gatewayHeaderInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/doc.html", "/webjars/**", "/swagger-resources/**", "/v2/api-docs"); // 放行文档
    }
}
