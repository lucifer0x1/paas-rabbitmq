package com.eco.paas.rabbitmq;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @ClassName MQProducer
 * @Author lucifer
 * @Date 4/11/19:10:51 AM
 * @Description
 */
@Component
public class MQProducer {

    @Autowired
    private AmqpTemplate template;

    @Autowired
    private AmqpAdmin admin;


    /**
     * @return void
     * @author lucifer wangxiyue.xy@163.com
     * @date 4/11/19
     * @para[routingKey 路由关键字, msg 消息体]
     * @功能说明: 发送内容
     **/
    public void sendMsg(String routingKey, String msg) {
        template.convertAndSend(routingKey, msg);
    }

    /**
     * @return void
     * @author lucifer wangxiyue.xy@163.com
     * @date 4/11/19
     * @para[exchange 交换机, routingKey 路由关键字, msg 消息体]
     * @功能说明:
     **/
    public void sendMsg(String exchange, String routingKey, String msg) {
        template.convertAndSend(exchange, routingKey, msg);
    }

    /**
     * @return void
     * @author lucifer wangxiyue.xy@163.com
     * @date 4/11/19
     * @param[routeKey 交换机, msg 消息体, map 消息头信息（消息属性）]
     * @功能说明:
     **/
    public void sendMsgModifyHead(String routeKey, String msg, Map<String, Object> map) {
        template.convertAndSend(null, routeKey, msg, message -> {
            message.getMessageProperties().getHeaders().putAll(map);
            return message;
        });
    }

    public void log(MSGHead head) {
        template.convertAndSend(null, "algPrint", "", message -> {
            message.getMessageProperties().getHeaders().putAll(head.getMSG());
            return message;
        });
    }

    /**
     * @return void
     * @author lucifer wangxiyue.xy@163.com
     * @date 4/11/19
     * @param[routeKey 交换机, msg 消息体, map 消息头信息（消息属性）]
     * @功能说明:
     **/
    public void sendMsgModifyHead(String routeKey, MSGHead head) {
        template.convertAndSend(null, routeKey, "", message -> {
            message.getMessageProperties().getHeaders().putAll(head.getMSG());
            return message;
        });
    }

}
