package com.eco.paas.rabbitmq.pojo;

import java.io.Serializable;
import java.util.Date;

/**
 * @auther lucifer
 * @mail wangxiyue.xy@163.com
 * @date 2021-01-12 16:29
 * @projectName paas-rabbitmq
 * @description:
 */
public class MsgPojo implements Serializable {


    private String msgId;
    private String exchangeName;
    private String routeKey;
    private String content;
    private MsgStatus status;
    private Date insertTime;//消息入库时间
    private Date expireTime;//消息过期时刻
    private Integer targetCount;//队列个数，或接收端个数，默认多少个消息被接收

    public MsgPojo(){
        insertTime = new Date();
    }

    public MsgPojo(String msgId,String exchangeName,String routeKey ,String content){
        this.msgId = msgId;
        this.exchangeName = exchangeName;
        this.routeKey  = routeKey;
        this.content = content;
        insertTime = new Date();
    }

    public MsgPojo(String msgId,String exchangeName, String routeKey, String content, MsgStatus status, Date insertTime, Date expireTime) {
        this.msgId = msgId;
        this.exchangeName = exchangeName;
        this.routeKey = routeKey;
        this.content = content;
        this.status = status;
        this.insertTime = insertTime;
        this.expireTime = expireTime;
    }

    /**
     * @author lucifer 2021-01-13 13:50
     * @description:
     * <p>功能: keyPrefix : exchange : route : status : msgId </p>
     */
    public String toRedisKey(String keyPrefix){
        StringBuilder sb  =new StringBuilder();
        sb.append(keyPrefix)
                .append(":").append(exchangeName)
                .append(":").append(routeKey)
                .append(":").append(status)
                .append(":").append(msgId);
        return sb.toString();
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public Integer getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(Integer targetCount) {
        this.targetCount = targetCount;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getRouteKey() {
        return routeKey;
    }

    public void setRouteKey(String routeKey) {
        this.routeKey = routeKey;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MsgStatus getStatus() {
        return status;
    }

    public void setStatus(MsgStatus status) {
        this.status = status;
    }

    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }
}
