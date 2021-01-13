package com.eco.paas.rabbitmq.pojo.annotation;

import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @auther lucifer
 * @mail wangxiyue.xy@163.com
 * @date 2021-01-12 20:10
 * @projectName paas-rabbitmq
 * @description:  消息监听消息确认
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SecurityMQReceiver {

    String exchange();

    String route();

    /**
     * 队列名称
     */
    String queue();
    
}
