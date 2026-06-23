package com.taotie.interceptor;

import com.taotie.context.BaseContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Component
public class FeignRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        // =============== 1. 传递用户ID（你写的，非常棒，保留） ===============
        Long currentId = BaseContext.getCurrentId();
        if (currentId != null) {
            template.header("X-User-Id", currentId.toString());
        }

        // =============== 2. 补上遗漏的“网关安全防伪标签” ===============
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            // 从当前请求中捞出网关塞进来的暗号
            String internalSecret = request.getHeader("X-Internal-Secret");

            // 如果有暗号，顺着 Feign 密传给下一个微服务
            if (internalSecret != null) {
                template.header("X-Internal-Secret", internalSecret);
            }
        }
    }
}