package com.eco.paas.rabbitmq.pojo.annotation;


import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @auther lucifer
 * @mail wangxiyue.xy@163.com
 * @date 2021-01-12 13:41
 * @projectName paas-rabbitmq
 * @description:
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SecurityMQSender {

    String id() default "null";

    /**
     * @author lucifer 2021-01-12 13:58
     * @description:
     * <p>功能: 交换</p>
     */
    String exchangeName();

    /**
     * @author lucifer 2021-01-12 13:58
     * @description:
     * <p>功能: 路由 </p>
     */
    String routeKey();

    /**
     * @author lucifer 2021-01-12 13:58
     * @description:
     * <p>功能: 消息内容</p>
     */
    String content();

    /**
     * @author lucifer 2021-01-12 14:44
     * @description:
     * <p>功能: 消息持久化过期时间</p>‘
     * @see #unit()
     */
    long ttl() default 1;
    /**
     * @author lucifer 2021-01-12 14:44
     * @description:
     * <p>功能: 默认过期 时间 1小时</p>
     */
    TimeUnit unit() default TimeUnit.HOURS;


}
