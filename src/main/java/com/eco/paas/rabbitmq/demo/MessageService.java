package com.eco.paas.rabbitmq.demo;

import com.eco.paas.rabbitmq.MQConsumer;
import com.eco.paas.rabbitmq.RabbitMQConfig;
import com.eco.paas.rabbitmq.pojo.annotation.SecurityMQReceiver;
import com.rabbitmq.client.Channel;
import jdk.nashorn.internal.objects.annotations.Constructor;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @ClassName MessageService
 * @Author lucifer
 * @Date 4/12/19:10:17 AM
 * @Description
 */
@Component
public class MessageService extends MQConsumer {


    @Autowired
    RabbitMQConfig rabbitMQConfig;

    /**
     * @param message :
     * @return : void
     * @author lucifer wangxiyue.xy@163.com
     * @date 4/12/19
     * @功能说明:
     **/
//    @RabbitListener(queues = "test_queue")
    public void algPrint(Message message) {
        if (message!=null){
            MSGHead head = new MSGHead(message.getMessageProperties().getHeaders());
            head.setCONTENT( new String(message.getBody()));
            System.out.println(head.toJSONSTR());
        }
    }

    @Override
    @SecurityMQReceiver(exchange = "text_exchange",
            route = "test_route",queue = "test_queue")
    public void onMsg(Message msg) {
        if (msg!=null){
            MSGHead head = new MSGHead(msg.getMessageProperties().getHeaders());
            head.setCONTENT( new String(msg.getBody()));
            System.out.println(head.toJSONSTR());
        }

    }
}
