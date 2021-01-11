package com.eco.paas.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.NoBackOffPolicy;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import javax.annotation.PostConstruct;
import java.util.UUID;

/**
 * @ClassName RabbitMQUtils
 * @Author lucifer
 * @Date 4/11/19:10:36 AM
 * @Description
 */
@Configuration
public class RabbitMQConfig {

    @Autowired
    AmqpAdmin amqpAdmin;

    private static SimpleMessageListenerContainer simpleMessageListenerContainer;


    public static String queueName = "test_queue";

    public static String exchangeName = "test_exchange";

    public static String routeKey = "test_route";

    public Queue localQueue(){
        return  new Queue(queueName,true,false,true);
    }

    public TopicExchange localTopicExchange(){
        return new TopicExchange(exchangeName);
    }

    public Binding bind(){
        return BindingBuilder.bind(localQueue()).to(localTopicExchange()).with(routeKey);
    }


    @PostConstruct
    public void init(){
        Queue queue = localQueue();
        TopicExchange topicExchange = localTopicExchange();
        amqpAdmin.deleteQueue(queueName,true,false);
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareExchange(topicExchange);
        amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(topicExchange).with(routeKey));
        System.out.println("create queue,exchange");
        simpleMessageListenerContainer.setAmqpAdmin(amqpAdmin);
    }


    /**
     * @author lucifer 2021-01-11 19:38
     * @description:
     * <p>功能: MQ 心跳</p>
     */
    private static class HeartBeat implements Runnable  {

        private AmqpAdmin amqpAdmin;
        private RabbitTemplate rabbitTemplate;

        HeartBeat(AmqpAdmin amqpAdmin){
            this.amqpAdmin = amqpAdmin;
            rabbitTemplate = new RabbitTemplate();
        }

        @Override
        public void run() {
            simpleMessageListenerContainer.setAdviceChain(createRetry());
        }

        private RetryOperationsInterceptor createRetry() {
            RetryTemplate retryTemplate = new RetryTemplate();
            retryTemplate.registerListener(new RetryListener() {
                @Override
                public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
                    // 第一次重试调用
                    return false;
                }

                @Override
                public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                    // 最后一次重试会调用
                }

                @Override
                public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                    // 每次重试失败后都会调用
                }
            });
            retryTemplate.setRetryPolicy(new SimpleRetryPolicy(Integer.MAX_VALUE));
            retryTemplate.setBackOffPolicy(new NoBackOffPolicy());
            return RetryInterceptorBuilder.stateless()
                    .retryOperations(retryTemplate)
                    .recoverer(new RepublishMessageRecoverer(rabbitTemplate))
                    .build();
        }
    }


}


