package com.taotie.interceptor;

import com.taotie.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC; // 🌟 引入日志诊断上下文依赖
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class GatewayHeaderInterceptor implements HandlerInterceptor {

    // 🌟 与网关约定的内部接头暗号
    private static final String SECRET_HEADER = "X-Internal-Secret";
    private static final String SECRET_VALUE = "bigold-secret-999999";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 🛡️ 【第一道防线：安全校验】先看是不是网关送过来的
        String secret = request.getHeader(SECRET_HEADER);
        if (secret == null || !secret.equals(SECRET_VALUE)) {
            log.error("🚨 警告：检测到非法请求绕过网关突袭！请求路径:{}，来源IP:{}", request.getRequestURI(), request.getRemoteAddr());

            // 拒绝访问，返回 403
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\": 0, \"msg\": \"Access Denied: 内部服务禁止直接访问！\"}");
            return false;
        }

        // 2. 👤 【第二道防线：获取用户信息】
        // 🌟 修正：改成 X-User-Id，与你网关 AuthGlobalFilter 里的自研请求头完美对应
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr != null && !userIdStr.isEmpty()) {
            Long userId = Long.valueOf(userIdStr);
            BaseContext.setCurrentId(userId);
            log.info("👤 收到网关透传的用户ID: {}，已存入当前线程上下文。", userId);
        }

        // 3. 🛰️ 【🌟 阶段 5 新增：第三道防线：捕获链路 TraceId】
        // 从 HTTP 请求头里揪出网关生成的全网唯一 X-Trace-Id
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId != null) {
            // 塞入 MDC（底层是 ThreadLocal），后续当前线程打印的所有业务日志都会自动抓取它
            MDC.put("traceId", traceId);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 4. 🧹 【第四道防线：打扫战场】不管业务成功还是抛异常，请求结束立刻清空 ThreadLocal 和 MDC！

        // 彻底移出用户信息
        Long userId = BaseContext.getCurrentId();
        if (userId != null) {
            BaseContext.removeCurrentId();
        }

        // 🌟【阶段 5 新增】彻底移出日志追踪上下文，防止线程复用导致日志串号
        String traceId = MDC.get("traceId");
        if (traceId != null) {
            MDC.remove("traceId");
            log.info("🧹 [ThreadLocal/MDC 清理成功] 线程 {} 的用户与追踪上下文已彻底移除，战场打扫完毕。", Thread.currentThread().getName());
        }
    }
}
