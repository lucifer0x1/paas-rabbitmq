package com.eco.paas.rabbitmq;

import com.eco.paas.rabbitmq.demo.MSGHead;
import com.eco.paas.rabbitmq.pojo.MsgPojo;
import com.eco.paas.rabbitmq.pojo.MsgStatus;
import com.eco.paas.rabbitmq.pojo.annotation.SecurityMQReceiver;
import com.eco.paas.redis.RedisType;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareBatchMessageListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static com.eco.paas.rabbitmq.pojo.MQConstants.HEADER_MESSAGE_POJO_ID_KEY;

/**
 * @auther lucifer
 * @mail wangxiyue.xy@163.com
 * @date 2021-01-11 17:09
 * @projectName paas-rabbitmq
 * @description:
 */
public abstract class MQConsumer implements ChannelAwareBatchMessageListener, BeanPostProcessor {

    Logger log = LoggerFactory.getLogger(MQConsumer.class);

    @Autowired
    private HashMap<RedisType, RedisTemplate<String,Object>> redisTemplateDefault;

    @Autowired
    private SimpleMessageListenerContainer container;

    public Message  recv(Message message) {
        onMessage(message);
        return message;
    }

    public abstract void onMsg(Message msg);

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        container.setMessageListener(new ChannelAwareBatchMessageListener() {
            @Override
            public void onMessageBatch(List<Message> messages, Channel channel) {
            }
        });


        return bean;
    }

    @Override
    public void onMessageBatch(List<Message> messages, Channel channel){
        MsgPojo msgPojo = new MsgPojo();
        /**
         * @author lucifer 2021-01-13 17:42
         * @description:
         * <p>功能: 补充获取 exchange rootkey   </p>
         */

        messages.forEach(m-> {
            try {
                onMsg(m);
                channel.basicNack(m.getMessageProperties().getDeliveryTag(),
                        false,false);
                msgPojo.setMsgId(m.getMessageProperties().getHeader(HEADER_MESSAGE_POJO_ID_KEY));


//              MsgPojo  oldPojo = (MsgPojo) redisTemplateDefault.get(RedisType.Default).opsForValue().get(msgPojo.toRedisKey(keyPrefix));
//                boolean flag = redisTemplateDefault.get(RedisType.Default).delete(msgPojo.toRedisKey(keyPrefix));
//                if(!flag){
//                    log.error("[From Redis]清理持久化消息失败");
//                }
                log.debug("处理消息[{}] 成功",msgPojo.getMsgId());
            } catch (IOException e) {
                log.debug("消息队列中无{}消息，或已被确认==>{}",
                        m.getMessageProperties().getDeliveryTag(),e.getMessage());
            } catch (Throwable throwable) {
                log.debug("消息处理失败，不处理该消息==>{}",throwable.getMessage());
            }
        });

    }
}
