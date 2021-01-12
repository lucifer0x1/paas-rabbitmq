package com.eco.paas.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @ClassName RabbitMQUtils
 * @Author lucifer
 * @Date 4/11/19:10:36 AM
 * @Description
 */
@Configuration
public class RabbitMQConfig {

    Logger log  = LoggerFactory.getLogger(RabbitMQConfig.class);

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    private CachingConnectionFactory cachingConnectionFactory;

    private static SimpleMessageListenerContainer simpleMessageListenerContainer;

    public static String queueName = "test_queue";

    public static String exchangeName = "test_exchange";

    public static String routeKey = "test_route";

    @Bean
    public Queue localQueue(){
        return  new Queue(queueName,true,false,true);
    }

    @Bean
    public TopicExchange localTopicExchange(){
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding bind(){
        return BindingBuilder.bind(localQueue()).to(localTopicExchange()).with(routeKey);
    }

    @PostConstruct
    public void init(){
        log.debug("Config 初始化");
        Queue queue = localQueue();
        TopicExchange topicExchange = localTopicExchange();
        amqpAdmin.deleteQueue(queueName,true,false);
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareExchange(topicExchange);
        amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(topicExchange).with(routeKey));
        log.debug("create queue,exchange");

    }



}


