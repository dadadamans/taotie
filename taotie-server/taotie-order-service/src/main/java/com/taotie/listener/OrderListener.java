package com.taotie.listener;

import com.rabbitmq.client.Channel;
import com.taotie.entity.Orders;
import com.taotie.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderListener {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 监听主业务订单队列
     * @param orderIdStr 接收到的消息体（这里发送端传的是订单的自增ID Long）
     * @param channel 当前的信道对象（手动 Ack 签收时必须使用）
     * @param tag 消息的唯一投递标签（Delivery Tag），RabbitMQ 用它来识别是哪条消息被处理了
     */
    @RabbitListener(queues = "order.queue")
    public void handleOrderCreated(String orderIdStr, Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            Long orderId = Long.valueOf(orderIdStr);
            log.info("收到下单消息，订单ID: {}，开始异步处理", orderId);

            Orders order = orderMapper.getById(orderId);
            if (order == null) {
                log.warn("订单不存在，ID: {}", orderId);
                channel.basicAck(tag, false);
                return;
            }

            // 预留异步处理扩展点
            // 1. 发送短信通知
            // 2. 增加用户积分
            // 3. 更新报表统计

            log.info("订单 {} 异步处理完成", orderId);
            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("消息处理失败，进入死信队列", e);
            try {
                channel.basicNack(tag, false, false);
            } catch (Exception ignored) {
            }
        }
    }
}
