package com.eco.paas.redis;

/**
 * @auther lucifer
 * @mail wangxiyue.xy@163.com
 * @date 2021-01-12 18:35
 * @projectName paas-rabbitmq
 * @description:
 */

public enum RedisType{
    Default("GenericJackson2JsonRedisSerializer"),
    Jdk("JdkSerializationRedisSerializer");

    private String descript;

    RedisType(String descript){
        this.descript = descript;
    }

    @Override
    public String toString() {
        return "RedisType{" +
                "descript='" + descript + '\'' +
                '}';
    }
}