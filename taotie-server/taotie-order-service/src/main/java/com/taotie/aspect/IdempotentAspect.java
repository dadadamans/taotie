package com.taotie.aspect;

import com.taotie.annotation.Idempotent;
import com.taotie.exception.BaseException;
import com.taotie.context.BaseContext; // 确保引入了饕餮外卖的 ThreadLocal 上下文
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Slf4j
public class IdempotentAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 1. 只要方法上贴了 @Idempotent 注解，就会自动触发下面这个环绕拦截方法
    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {

        // 2. 从 Spring 的底层上下文中，强行捞出当前这次网络请求的 Request 对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed(); // 如果拿不到请求（比如内部调用），直接放行
        }
        HttpServletRequest request = attributes.getRequest();

        // 3. 尝试去前端发来的请求头（Headers）里面，提取叫 "Source-ID" 的身份证
        String sourceId = request.getHeader("Source-ID");

        // 4. 【🌟 核心兼容性改动】如果前端没有传 Source-ID，开启后端自动兜底防重机制
        if (!StringUtils.hasText(sourceId)) {
            log.info("【幂等提示】前端未传入 Source-ID，自动激活后端 Token/用户ID 兜底防重机制");

            // 方案 A：尝试获取前端请求头里必带的登录凭证 authentication (Token)
            sourceId = request.getHeader("authentication");

            // 方案 B：如果连 Token 都拿不到，直接用当前登录大厨/用户的 ThreadLocal ID 焊死防重
            if (!StringUtils.hasText(sourceId)) {
                try {
                    Long currentId = BaseContext.getCurrentId();
                    if (currentId != null) {
                        sourceId = "USER_" + currentId;
                    }
                } catch (Exception e) {
                    log.error("尝试获取当前登录用户ID失败: {}", e.getMessage());
                }
            }
        }

        // 4.5 如果连兜底的身份标识都榨不出来，才彻底拦截报错
        if (!StringUtils.hasText(sourceId)) {
            log.warn("幂等拦截失败：请求中缺少任何可识别的身份标示(Header/Token/ContextID)");
            throw new BaseException("请求无效，缺少唯一流水号(Source-ID)");
        }

        // 5. 将捞出来的唯一标示拼接成 Redis 的 Key
        //    如果是连击，前后两次请求的 Token 或用户 ID 绝对一模一样，对应的 redisKey 也就完全相同！
        String redisKey = "idempotent:lock:" + sourceId;

        // 6. 【第二道关卡】核心抢占：利用 Redis 的 setIfAbsent (对应底层命令 SETNX)
        Boolean success = redisTemplate.opsForValue().setIfAbsent(
                redisKey,
                "1",
                idempotent.timeout(),
                idempotent.timeUnit()
        );

        // 7. 如果返回 false，说明上一次请求的锁还没过期呢，判定为“连击/重复提交”，直接抛异常轰走
        if (Boolean.FALSE.equals(success)) {
            log.warn("🔥【幂等拦截成功】触发防重！检测到重复请求，Key为: {}", redisKey);
            throw new BaseException(idempotent.message()); // 弹出你在注解里写的提示信息
        }

        // 8. 抢占成功，控制台打印日志记录
        log.info("🎯【幂等锁加锁成功】放行本次请求，Key为: {}", redisKey);

        try {
            // 9. 【安全放行】这行代码会真正去执行 Controller 里的下单或支付业务逻辑
            return joinPoint.proceed();

        } catch (Throwable throwable) {
            // 10. 【异常回退】如果后面的下单业务报错了（比如商品库存不足、用户余额不够）
            //     必须立刻把 Redis 里的锁删掉！否则用户修改购物车后，再次提交还会被无辜卡死。
            redisTemplate.delete(redisKey);
            log.info("业务执行异常，已自动释放 Redis 幂等锁, Key为: {}", redisKey);

            // 11. 继续把业务异常往外抛
            throw throwable;
        }
    }
}