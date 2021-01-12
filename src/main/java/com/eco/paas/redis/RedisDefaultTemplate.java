package com.eco.paas.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.HashMap;

/**
 * @auther lucifer
 * @mail wangxiyue.xy@163.com
 * @date 2021-01-12 18:28
 * @projectName paas-rabbitmq
 * @description:
 */
@Configuration
@Order
public class RedisDefaultTemplate {

    @Bean("redisTemplateDefault")
    public HashMap<RedisType, RedisTemplate<String,Object>> redisTemplateDefault(RedisConnectionFactory redisConnectionFactory) {
        HashMap<RedisType,RedisTemplate<String,Object>> res = new HashMap<>();
        RedisTemplate<String,Object> redisTemplate1 = new RedisTemplate<>();
        redisTemplate1.setConnectionFactory(redisConnectionFactory);
        redisTemplate1.setKeySerializer(new StringRedisSerializer());
        //如果用其他的方式存储会被序列化成JAVA对象,无法操作
        redisTemplate1.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate1.afterPropertiesSet();
        res.put(RedisType.Default,redisTemplate1);

        RedisTemplate<String,Object> redisTemplate2 = new RedisTemplate<>();
        redisTemplate2.setConnectionFactory(redisConnectionFactory);
        redisTemplate2.setKeySerializer(new StringRedisSerializer());
        //如果用其他的方式存储会被序列化成JAVA对象,无法操作
        redisTemplate2.setValueSerializer(new JdkSerializationRedisSerializer());
        redisTemplate2.afterPropertiesSet();
        res.put(RedisType.Jdk,redisTemplate2);
        return res;
    }
}

