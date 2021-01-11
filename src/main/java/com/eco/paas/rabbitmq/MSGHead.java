package com.eco.paas.rabbitmq;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName MSGHead
 * @Author lucifer
 * @Date 4/11/19:4:58 PM
 * @Description
 */
public class MSGHead {

    //消息ID 消息唯一性
    private String MSGID;
    //消息保存时间
    private String TIME;
    //消息所属算法应用ID ，消息制造者标识
    private String ALGVER;
    //消息级别 info，
    private String LEVEL;
    //流程ID FID，
    private String FID;
    //消息内容
    private String CONTENT;

    private Map<String, Object> msg = new HashMap<>();

    public MSGHead(Map<String, Object> srcMsg) {
        this.MSGID = String.valueOf(srcMsg.get("MSGID"));
        this.TIME = String.valueOf(srcMsg.get("TIME"));
        this.ALGVER = String.valueOf(srcMsg.get("ALGVER"));
        this.LEVEL = String.valueOf(srcMsg.get("LEVEL"));
        this.CONTENT = String.valueOf(srcMsg.get("CONTENT"));
        this.FID = String.valueOf(srcMsg.get("FID"));
        getMSG();
    }

    private MSGLevel obj2level(Object msg){
        MSGLevel msgLevel = MSGLevel.INFO;
        if(msg!=null){
           String strL = String.valueOf(msg);
            for (MSGLevel L : MSGLevel.values()) {
                if(L.toString().equals(strL)){
                    msgLevel = L;
                    break;
                }
            }
        }
        return msgLevel;
    }

    public MSGHead(String MSGID, String TIME, String ALGVER, String LEVEL, String CONTENT,String FID) {
        this.MSGID = MSGID;
        this.TIME = TIME;
        this.ALGVER = ALGVER;
        this.LEVEL = LEVEL;
        this.CONTENT = CONTENT;
        this.FID = FID;
        getMSG();
    }

    public Map<String, Object> getMSG() {
        msg.put("MSGID", MSGID);
        msg.put("TIME", TIME);
        msg.put("ALGVER", ALGVER);
        msg.put("LEVEL", LEVEL);
        msg.put("CONTENT", CONTENT);
        msg.put("FID", FID);
        return msg;
    }

    public String getMSGID() {
        return MSGID;
    }

    public void setMSGID(String MSGID) {
        this.MSGID = MSGID;
    }

    public String getTIME() {
        return TIME;
    }

    public void setTIME(String TIME) {
        this.TIME = TIME;
    }

    public String getALGVER() {
        return ALGVER;
    }

    public void setALGVER(String ALGVER) {
        this.ALGVER = ALGVER;
    }

    public String getLEVEL() {
        return LEVEL;
    }

    public void setLEVEL(String LEVEL) {
        this.LEVEL = LEVEL;
    }

    public String getCONTENT() {
        return CONTENT;
    }

    public void setCONTENT(String CONTENT) {
        this.CONTENT = CONTENT;
    }

    public String getFID() {
        return FID;
    }

    public void setFID(String FID) {
        this.FID = FID;
    }

    public Map<String, Object> getMsg() {
        return msg;
    }

    public void setMsg(Map<String, Object> msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        String format = "" +
                "MSGID = [%s]\n" +
                "TIME = [%s]\n" +
                "ALGVER = [%s]\n" +
                "LEVEL = [%s]\n" +
                "CONTENT = [%s]\n"+
                "FID = [%s]\n";
        return String.format(format, MSGID, TIME, ALGVER, LEVEL, CONTENT,FID);

    }

    /**
     * @author lucifer wangxiyue.xy@163.com
     * @date 7/10/19
     * @return : java.lang.String
     * @功能说明: 将msg 消息信息体 转换成JSON 格式输出
     *          用于发送到  websocket 客户端
     **/
    /*public String toJSONSTR(){
        String format = "{" +
                "\"MSGID\": [%s] ,\n" +
                "\"TIME\": [%s] ,\n" +
                "\"ALGVER\": [%s] ,\n" +
                "\"LEVEL\": [%s] ,\n" +
                "\"CONTENT\": [%s]\n" +
                "\"FID\": [%s]\n" +
                "}";
        return String.format(format, MSGID, TIME, ALGVER, LEVEL, CONTENT,FID);
    }*/
    public String toJSONSTR(){
        String format = "{" +
                "\"MSGID\": \"%s\"," +
                "\"TIME\": \"%s\"," +
                "\"ALGVER\": \"%s\"," +
                "\"LEVEL\": \"%s\"," +
                "\"CONTENT\":%s," +
                "\"FID\": \"%s\"" +
                "}";
        return String.format(format, MSGID, TIME, ALGVER, LEVEL, CONTENT,FID);
    }

   /* public MessageModel toModel() {
        MessageModel model = new MessageModel(getMSGID(), getTIME(), getALGVER(),
                getLEVEL().name(), getCONTENT(), new Date(),getFID());
        return model;
    }*/

}


