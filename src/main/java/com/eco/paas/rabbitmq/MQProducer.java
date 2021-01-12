package com.eco.paas.rabbitmq;

import com.eco.paas.rabbitmq.pojo.annotation.SecurityMQSender;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @ClassName MQProducer
 * @Author lucifer
 * @Date 4/11/19:10:51 AM
 * @Description  消息持久化
 */
@Component
public class MQProducer {

    @Autowired
    private AmqpTemplate template;

    /**
     * @return void
     * @author lucifer wangxiyue.xy@163.com
     * @date 4/11/19
     * @para[exchange 交换机, routingKey 路由关键字, msg 消息体]
     * @功能说明:
     **/
    @SecurityMQSender(exchangeName = "#exchange",routeKey = "#routingKey",content = "#msg")
    public void sendMsg(String exchange, String routingKey, String msg) {
        template.convertAndSend(exchange, routingKey, msg);
    }

    /**
     * @return void
     * @author lucifer wangxiyue.xy@163.com
     * @date 4/11/19
     * @para[exchange 交换机, routingKey 路由关键字, msg 消息体]
     * @功能说明:
     **/
    public void sendMsg(String exchange, String routingKey, Message msg) {
        template.convertAndSend(exchange, routingKey, msg);
    }

    /**
     * @return void
     * @author lucifer wangxiyue.xy@163.com
     * @date 4/11/19
     * @param[routeKey 交换机, msg 消息体, map 消息头信息（消息属性）]
     * @功能说明:
     **/
    public void sendMsgModifyHead(String exchange,String routeKey, String msg, Map<String, Object> map) {
        template.convertAndSend(exchange, routeKey, msg, message -> {
            message.getMessageProperties().getHeaders().putAll(map);
            return message;
        });
    }

    public void log(MSGHead head) {
        template.convertAndSend("algPrint", "algPrint", "", message -> {
            message.getMessageProperties().getHeaders().putAll(head.getMSG());
            return message;
        });
    }

}
