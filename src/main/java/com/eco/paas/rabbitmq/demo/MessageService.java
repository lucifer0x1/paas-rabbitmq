package com.eco.paas.rabbitmq.demo;

import com.eco.paas.rabbitmq.MQConsumer;
import com.eco.paas.rabbitmq.RabbitMQConfig;
import com.eco.paas.rabbitmq.pojo.annotation.SecurityMQReceiver;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
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
        }
    }

    @Override
    @SecurityMQReceiver(exchange = "test_exchange",
            route = "test_route",queue = "test_queue")
    public void onMsg(Message msg) {
        if (msg!=null){
            MSGHead head = new MSGHead(msg.getMessageProperties().getHeaders());
            head.setCONTENT( new String(msg.getBody()));
            System.out.println(head.toJSONSTR());
        }

    }
}
