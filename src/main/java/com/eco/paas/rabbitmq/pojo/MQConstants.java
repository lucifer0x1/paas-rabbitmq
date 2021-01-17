package com.eco.paas.rabbitmq.pojo;

/**
 * @auther lucifer
 * @mail wangxiyue.xy@163.com
 * @date 2021-01-13 14:43
 * @projectName paas-rabbitmq
 * @description:
 */
public class MQConstants {

    /**
     * @author lucifer 2021-01-13 14:44
     * @description:
     * <p>功能: 消息持久化 头ID </p>
     */
    public final static String HEADER_MESSAGE_POJO_ID_KEY="MSG-ID";

    /**
     * @author lucifer
     * @description:
     * 消息重发时间界定: redis中的消息超过多少 毫秒 的消息需要重发,默认值60秒
     */
    public final static long MESSAGE_RESEND_SECOND=180000;
}
