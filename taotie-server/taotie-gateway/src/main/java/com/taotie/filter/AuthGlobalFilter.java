package com.taotie.filter;

import com.taotie.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.UUID; // 🌟 引入 UUID

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final String adminSecretKey = "itcast";
    private final String userSecretKey = "itheima";

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final List<String> whiteList = Arrays.asList(
            "/admin/employee/login",
            "/user/user/login",
            "/user/shop/status",
            "/user/category/list",
            "/user/dish/list",
            "/user/setmeal/list",
            "/user/setmeal/dish/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 🌟【阶段 5 新增：全链路追踪起点】
        // 无论这个请求是白名单还是需要登录，一进大门，先发一张全网唯一的链路追踪身份证！
        String traceId = "trace-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        // 先把 TraceId 塞进请求头构建器里，下面后续的所有修改都基于这个 mutatedRequest
        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();
        requestBuilder.header("X-Trace-Id", traceId); // 塞入链路 ID

        // 后续代码中如果有放行或修改请求的地方，全部统一使用修改后的 requestBuilder

        if (request.getMethod() == org.springframework.http.HttpMethod.OPTIONS) {
            System.out.println("[" + traceId + "] 放行CORS预检请求：" + path);
            // 🌟 带有 TraceId 转发
            return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
        }

        boolean isWhiteList = whiteList.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
        if (isWhiteList) {
            System.out.println("[" + traceId + "] 放行白名单接口：" + path);
            // 🌟 带有 TraceId 转发
            return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
        }

        boolean isAdmin = path.startsWith("/admin/");
        String token = request.getHeaders().getFirst(isAdmin ? "token" : "authentication");

        if (token == null || token.isEmpty()) {
            System.out.println("[" + traceId + "] 拦截非法请求：未携带 Token -> " + path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String userIdStr;
        try {
            if (isAdmin) {
                Claims claims = JwtUtil.parseJWT(adminSecretKey, token);
                userIdStr = claims.get("empId").toString();
                System.out.println("[" + traceId + "] 【员工端】Token校验通过，解密真实ID: " + userIdStr);
            } else {
                Claims claims = JwtUtil.parseJWT(userSecretKey, token);
                userIdStr = claims.get("userId").toString();
                System.out.println("[" + traceId + "] 【用户端】Token校验通过，解密真实ID: " + userIdStr);
            }
        } catch (Exception e) {
            System.out.println("[" + traceId + "] 拦截非法请求：Token 校验失败！错误原因: " + e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 🌟 校验成功，把用户ID也塞进请求头构建器
        requestBuilder.header("X-User-Id", userIdStr);

        // 🌟 最终发射：同时带着 X-Trace-Id 和 X-User-Id 传给后面的微服务模块！
        return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}