package com.eco.paas.rabbitmq.pojo.aop;

import com.eco.paas.rabbitmq.pojo.MsgPojo;
import com.eco.paas.rabbitmq.pojo.MsgStatus;
import com.eco.paas.rabbitmq.pojo.annotation.SecurityMQReceiver;
import com.eco.paas.rabbitmq.pojo.annotation.SecurityMQSender;
import com.eco.paas.redis.RedisType;
import com.rabbitmq.client.Channel;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareBatchMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.security.auth.kerberos.KerberosKey;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import static com.eco.paas.rabbitmq.pojo.MQConstants.HEADER_MESSAGE_POJO_ID_KEY;

/**
 * @author lucifer
 * @mail wangxiyue.xy@163.com
 * @date 2021-01-12 13:21
 * @projectName paas-rabbitmq
 * @description: 消息持久化Aop ， 拦截RabbitListener 切面， 处理前消息持久化
 */
@Aspect
@Component
@DependsOn("redisTemplateDefault")
public class MessageCheckAop implements BeanPostProcessor {

    private ConcurrentHashMap<String, QueueInformation> queueCache  =new ConcurrentHashMap<>();

    Logger log = LoggerFactory.getLogger(MessageCheckAop.class);
    /**
     * @author lucifer 2021-01-12 15:30
     * @description:
     * <p>功能: 为特殊需求，不要修改 会影响其他服务获取不到持久化的消息 </p>
     */
    @Value("${com.eco.paas.rabbitmq.pojo.prefix:MessagePOJO}")
    private String keyPrefix;

    @Resource(name ="redisTemplateDefault")
    HashMap<RedisType, RedisTemplate<String,Object>> redisTemplateDefault;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private SimpleMessageListenerContainer container;

    @Autowired
    private AmqpAdmin amqpAdmin;

    SpelExpressionParser parser = new SpelExpressionParser();

    /**
     * 发送消息持久化切面
     */
    @Pointcut("@annotation(com.eco.paas.rabbitmq.pojo.annotation.SecurityMQSender)")
    private void senderPointCut(){}

    /**
     * 接收消息持久化切面(监听)
     */
    @Pointcut("@annotation(com.eco.paas.rabbitmq.pojo.annotation.SecurityMQReceiver)")
    private void receiverPointCut(){};

    @Around(value = "receiverPointCut()")
    public void aroud(ProceedingJoinPoint joinPoint, SecurityMQReceiver receiver){
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String exchangeName = receiver.exchange();
        String routeKey = receiver.route();
        String queue = receiver.queue();
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length != 0) {
            // 0-2、前提条件：拿到作为key的依据  - 解析springEL表达式
            EvaluationContext ctx = new StandardEvaluationContext();
            String[] parameterNames = signature.getParameterNames();
            for (int i = 0; i < parameterNames.length; i++) {
                ctx.setVariable(parameterNames[i], arrayToStr(args[i]));
            }
            exchangeName =parser.parseExpression(exchangeName).getValue(ctx).toString();
            routeKey =parser.parseExpression(routeKey).getValue(ctx).toString();
            queue =parser.parseExpression(queue).getValue(ctx).toString();
        }

        if(!queueCache.containsKey(queue)){
            if(amqpAdmin.getQueueInfo(queue)==null){
                amqpAdmin.deleteQueue(queue);
                amqpAdmin.declareBinding(BindingBuilder.bind(new Queue(queue))
                        .to(new TopicExchange(exchangeName)).with(routeKey));
            }
            bindListener(joinPoint,exchangeName,routeKey);
            queueCache.put(queue,amqpAdmin.getQueueInfo(queue));
        }
//
    }


    private void bindListener(ProceedingJoinPoint joinPoint,String exchange,String route){
        MsgPojo msgPojo = new MsgPojo();
        msgPojo.setStatus(MsgStatus.OK);
        msgPojo.setExchangeName(exchange);
        msgPojo.setRouteKey(route);
        container.setMessageListener(new ChannelAwareBatchMessageListener() {
            @Override
            public void onMessageBatch(List<Message> messages, Channel channel) {
                messages.forEach(m-> {
                    try {
                        joinPoint.proceed(new Object[]{m});
                        channel.basicNack(m.getMessageProperties().getDeliveryTag(),
                                false,false);
                        msgPojo.setMsgId(m.getMessageProperties().getHeader(HEADER_MESSAGE_POJO_ID_KEY));
//                        MsgPojo  oldPojo = (MsgPojo) redisTemplateDefault.get(RedisType.Default).opsForValue().get(msgPojo.toRedisKey(keyPrefix));
                        boolean flag = redisTemplateDefault.get(RedisType.Default).delete(msgPojo.toRedisKey(keyPrefix));
                        if(!flag){
                            log.error("[From Redis]清理持久化消息失败");
                        }
                        log.debug("处理消息[{}] 成功",msgPojo.getMsgId());
                    } catch (IOException e) {
                        log.debug("消息队列中无{}消息，或已被确认==>{}",
                                m.getMessageProperties().getDeliveryTag(),e.getMessage());
                    } catch (Throwable throwable) {
                        log.debug("消息处理失败，不处理该消息==>{}",throwable.getMessage());
                    }
                });
            }
        });
    }

    @Before("senderPointCut()")
    public void beforeSend(JoinPoint point){
        log.debug("发送消息前");
        MethodSignature signature = (MethodSignature) point.getSignature();
        SecurityMQSender sender  =  signature.getMethod().getAnnotation(SecurityMQSender.class);
        String exchangeName = sender.exchangeName();
        String routeKey = sender.routeKey();
        String content = sender.content();
        String msgId  = sender.id();

        log.debug("AOP ===> 参数处理");
        // 参数不为空时解析EL表达式
        Object[] args = point.getArgs();
        if (args != null && args.length != 0) {
            // 0-2、前提条件：拿到作为key的依据  - 解析springEL表达式
            EvaluationContext ctx = new StandardEvaluationContext();
            String[] parameterNames = signature.getParameterNames();
            for (int i = 0; i < parameterNames.length; i++) {
                ctx.setVariable(parameterNames[i], arrayToStr(args[i]));
            }
            exchangeName =parser.parseExpression(exchangeName).getValue(ctx).toString();
            routeKey =parser.parseExpression(routeKey).getValue(ctx).toString();
            content =parser.parseExpression(content).getValue(ctx).toString();
            if("null".equals(msgId)){
                msgId = UUID.randomUUID().toString().replaceAll("-","");
                log.error("当前发送未指定MessageID 可能会造成重复发送,暂时使用消息ID==>{}",msgId);
            }else {
                msgId =parser.parseExpression(msgId).getValue(ctx).toString();
            }
        }
        log.debug("组装消息");
        MsgPojo pojo  = new MsgPojo(msgId,exchangeName,routeKey,content);
        pojo.setStatus(MsgStatus.RUNNING);
        log.debug("Redis持久化");
        redisTemplateDefault.get(RedisType.Default).opsForValue().set(pojo.toRedisKey(keyPrefix), pojo , sender.ttl(), sender.unit());
        log.debug("Message Save Redis Complete==>exchange={},routekey={}",exchangeName,routeKey);
    }

    @PostConstruct
    public void init(){
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("HeartBeat ===> "+ r.getClass());
                return t;
            }
        });
        log.debug("开启心跳服务====>监控Redis 持久化消息");
        executorService.scheduleAtFixedRate(new HeartBeatPoll(keyPrefix,redisTemplateDefault.get(RedisType.Default),amqpTemplate),
                0,10, TimeUnit.SECONDS);
        log.debug("心跳服务正常运行");
    }

    /**
     * 数组转String
     * 例如：int[] i = new int[]{1, 2, 3};
     * 返回：[1, 2, 3]
     *
     * @param o 数组
     * @return str
     * @author Hash
     */
    private String arrayToStr(Object o) {
        if (o == null) {
            return null;
        }
        if (!o.getClass().isArray()) {
            return o.toString();
        }
        if (o instanceof byte[]) {
            return Arrays.toString((byte[]) o);
        } else if (o instanceof short[]) {
            return Arrays.toString((short[]) o);
        } else if (o instanceof int[]) {
            return Arrays.toString((int[]) o);
        } else if (o instanceof long[]) {
            return Arrays.toString((long[]) o);
        } else if (o instanceof float[]) {
            return Arrays.toString((float[]) o);
        } else if (o instanceof double[]) {
            return Arrays.toString((double[]) o);
        } else if (o instanceof char[]) {
            return Arrays.toString((char[]) o);
        } else {
            return o.toString();
        }
    }

    /**
     * @author lucifer 2021-01-12 15:01
     * @description:
     * <p>功能: 心跳监测过期消息</p>
     */
    private static class HeartBeatPoll implements Runnable {


        private String redisKeyPattern;
        Logger log  = LoggerFactory.getLogger(HeartBeatPoll.class);
        private final RedisTemplate<String,Object> redisTemplate;
        private final AmqpTemplate template;

        public HeartBeatPoll(String keyPrefix,RedisTemplate<String,Object> redisTemplate,AmqpTemplate amqpTemplate){
            redisKeyPattern  =keyPrefix +":*:*:" + MsgStatus.RUNNING + ":*";

            this.redisTemplate = redisTemplate;
            this.template = amqpTemplate;
            log.debug("心跳线程初始化===>{}","[ok]");
        }

        @Override
        public void run() {

            Set<String> keys = this.redisTemplate.keys(redisKeyPattern);
            List<Object> msgPojoList = this.redisTemplate.opsForValue().multiGet(keys);
            for (Object o : msgPojoList) {
                if(o instanceof MsgPojo){
                    MsgPojo msg = (MsgPojo)o;
                    template.convertAndSend(msg.getExchangeName(), msg.getRouteKey(), msg.getContent());
                    log.debug("重新发送消息 {} ===> {} 成功",msg.getExchangeName(),msg.getRouteKey());
                }else{
                    log.error("Redis持久化的消息结构不是 {}",MsgPojo.class);
                }
            }

            /**
             * @author lucifer 2021-01-12 17:01
             * @description:
             * <p>功能: 校验Key</p>
             */
//            String exchangeName;
//            String routeKey;
//            String content;
//            for (String key : keys) {
//                //keyPrefix : exchange  : routekey : RUNNING
//                if(key==null || key.length()< 7){
//                    continue;
//                }
//                String[] kSplit = key.split(":");
//                if(kSplit.length==4){
//                    exchangeName  = kSplit[1];
//                    routeKey = kSplit[2];
//                    content = this.redisTemplate.opsForValue().get(key);
//                }
//            }


        }


    }
}
