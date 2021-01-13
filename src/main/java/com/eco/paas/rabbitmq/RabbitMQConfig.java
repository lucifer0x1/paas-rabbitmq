package com.eco.paas.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.DirectRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${com.eco.paas.rabbitmq.queue:test_queue}")
    public String queueName = "test_queue";

    @Value("${com.eco.paas.rabbitmq.exchange:test_exchange}")
    public String exchangeName = "test_exchange";

    @Value("${com.eco.paas.rabbitmq.route:test_route}")
    public String routeKey = "test_route";

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

    @Bean
    public SimpleMessageListenerContainer SimpleMessageListenerContainer(ConnectionFactory connectionFactory){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory.createListenerContainer();
    }



}


