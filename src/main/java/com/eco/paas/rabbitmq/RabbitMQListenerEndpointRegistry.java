package com.eco.paas.rabbitmq;

import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @auther lucifer
 * @mail wangxiyue.xy@163.com
 * @date 2021-01-18 15:22
 * @projectName paas-rabbitmq
 * @description:
 * @see org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry
 */
@Component
public class RabbitMQListenerEndpointRegistry implements ApplicationContextAware {

    private ConfigurableApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if(applicationContext instanceof ConfigurableApplicationContext){
            this.applicationContext = (ConfigurableApplicationContext) applicationContext;
        }
    }

    public void registry(List<SimpleMessageListenerContainer> containers){
        for (SimpleMessageListenerContainer container : containers) {
            try {
                container.start();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        this.applicationContext.getBeanFactory().registerSingleton("security.mq.container",containers);
//        this.applicationContext.refresh();

    }
}
