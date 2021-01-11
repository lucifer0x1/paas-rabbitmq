package com.eco.paas.rabbitmq;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

/**
 * @auther lucifer
 * @mail wangxiyue.xy@163.com
 * @date 2021-01-11 17:09
 * @projectName paas-rabbitmq
 * @description:
 */
@Service
public class MessageReceiver   {

    @RabbitListener(queues="#{rabbitMQConfig.queueName}")
    @RabbitHandler
    public void recv(Message message) {
        if (message!=null){
            MSGHead head = new MSGHead(message.getMessageProperties().getHeaders());
            head.setCONTENT( new String(message.getBody()));
            System.out.println(head.toJSONSTR());
        }
    }

}
