package com.taotie.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfiguration {

    /**
     * 声明主业务交换机（定向模式 Direct）
     * 作用：接收订单服务发来的消息，并根据具体的 RoutingKey（路由键）精准投递到对应队列
     */
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange("order.exchange");
    }

    /**
     * 声明主业务队列（核心，带死信绑定）
     */
    @Bean
    public Queue orderQueue() {
        Map<String, Object> args = new HashMap<>();

        // 【关键配置 1】指定该队列的“死信交换机”。当这个队列的消息变成死信（比如被拒绝、消费失败）时，会把消息转发给它
        args.put("x-dead-letter-exchange", "order.exchange.dlx");

        // 【关键配置 2】指定死信消息转发出去时带着的“死信路由键”，用于死信交换机精准投递到死信队列
        args.put("x-dead-letter-routing-key", "order.created.dlq");

        // 参数解析：名称, durable(是否持久化), exclusive(是否独占), autoDelete(是否自动删除), arguments(额外参数)
        // 这里设置为持久化队列（true），配合之前说的消息持久化，确保 RabbitMQ 重启后队列不丢失
        return new Queue("order.queue", true, false, false, args);
    }

    /**
     * 绑定主队列到主交换机
     * 路由规则：只要发往 order.exchange 的消息带着 "order.created" 路由键，就会被送进 order.queue
     */
    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue())
                .to(orderExchange())
                .with("order.created");
    }

    /**
     * 声明死信交换机（DLX - Dead Letter Exchange）
     * 作用：它其实就是一个普通的直连交换机，只是专门用来承接主队列弹出来的垃圾/失败消息
     */
    @Bean
    public DirectExchange orderDlx() {
        return new DirectExchange("order.exchange.dlx");
    }

    /**
     * 声明死信队列（DLQ - Dead Letter Queue）
     * 作用：用来接收、堆积那些连续消费失败的异常订单消息，方便后续人工介入排查或启动补偿程序
     */
    @Bean
    public Queue orderDlq() {
        return new Queue("order.queue.dlq", true); // 第二个参数 true 代表队列持久化
    }

    /**
     * 绑定死信队列到死信交换机
     * 路由规则：对应上面主队列配置的 "order.created.dlq" 路由键
     */
    @Bean
    public Binding orderDlqBinding() {
        return BindingBuilder.bind(orderDlq())
                .to(orderDlx())
                .with("order.created.dlq");
    }
}
