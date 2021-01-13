package com.eco.paas.rabbitmq.pojo;

/**
 * @auther lucifer
 * @mail wangxiyue.xy@163.com
 * @date 2021-01-12 14:49
 * @projectName paas-rabbitmq
 * @description: pojo 消息状态
 */
public enum MsgStatus {

    OVERDUE("过期"),
    RUNNING("发送中"),
    OK("已处理");

    private String description;

    MsgStatus(String descript){
        description = descript;
    }

}
