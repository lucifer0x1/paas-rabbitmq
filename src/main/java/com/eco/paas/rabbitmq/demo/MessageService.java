package com.eco.paas.rabbitmq.demo;

import com.eco.paas.rabbitmq.MQConsumer;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @ClassName MessageService
 * @Author lucifer
 * @Date 4/12/19:10:17 AM
 * @Description
 */
@Component
public class MessageService extends MQConsumer {

    /**
     * @param message :
     * @return : void
     * @author lucifer wangxiyue.xy@163.com
     * @date 4/12/19
     * @功能说明:
     **/
    @RabbitListener(queues = "test_queue")
    public void algPrint(Message message) {
        if (message!=null){
            MSGHead head = new MSGHead(message.getMessageProperties().getHeaders());
            head.setCONTENT( new String(message.getBody()));
            System.out.println(head.toJSONSTR());
        }
    }

    @Override
    public void onMsg(Message msg) {
        if (msg!=null){
            MSGHead head = new MSGHead(msg.getMessageProperties().getHeaders());
            head.setCONTENT( new String(msg.getBody()));
            System.out.println(head.toJSONSTR());
        }

    }


}
