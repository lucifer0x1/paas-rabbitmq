package com.eco.paas.rabbitmq;

import com.eco.paas.rabbitmq.pojo.MsgPojo;
import com.eco.paas.rabbitmq.pojo.MsgStatus;
import com.eco.paas.rabbitmq.pojo.annotation.SecurityMQReceiver;
import com.eco.paas.redis.RedisType;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ReflectionUtils;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.eco.paas.rabbitmq.pojo.MQConstants.HEADER_MESSAGE_POJO_ID_KEY;

/**
 * @auther lucifer
 * @mail wangxiyue.xy@163.com
 * @date 2021-01-11 17:09
 * @projectName paas-rabbitmq
 * @description:
 * @see org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor
 *
 * TODO 目前不支持多个方法绑定，后面补充，需要EndingPoint 加入到 Spring 环
 */
public abstract class MQConsumer {


    Logger log = LoggerFactory.getLogger(MQConsumer.class);

    @Autowired
    private HashMap<RedisType, RedisTemplate<String,Object>> redisTemplateDefault;

    @Autowired
    private SimpleRabbitListenerContainerFactory factory;

    @Value("${com.eco.paas.rabbitmq.pojo.prefix:MessagePOJO}")
    private String keyPrefix;

    public abstract void onMsg(Message msg);

    ConcurrentLinkedQueue<QueueCacheObj> msgCache = new ConcurrentLinkedQueue<>();

    private static ScheduledExecutorService executorService;

    @Bean
    public SimpleMessageListenerContainer madeContainer() throws BeansException {
        List<SimpleMessageListenerContainer> containers = new ArrayList<>();
        /**
         * @author lucifer 2021-01-14 11:20
         * @description:
         * <p>功能: 交给Spring 托管Bean 时 需要获取全部注解方法 , 多个队列在一个对象中时</p>
         */
        final List<MetaListener> metaListeners = new ArrayList<>();
        ReflectionUtils.doWithMethods(this.getClass(), method -> {
            Collection<SecurityMQReceiver> listenerAnnotations = findListenerAnnotations(method);
            if (listenerAnnotations.size() > 0) {
                SecurityMQReceiver receiver = AnnotationUtils.findAnnotation(method, SecurityMQReceiver.class);
                metaListeners.add(new MetaListener(receiver,method));
            }
        }, ReflectionUtils.USER_DECLARED_METHODS);

        if(metaListeners.size()<=0){
            log.debug("没有发现[{}]注解类",SecurityMQReceiver.class);
            return null;
//            throw new BeanCreationNotAllowedException(this.getClass().getName(),"未获取 自定义注解 SecurityMQReceiver，不能绑定消息队列");
        }
        log.debug("目标  [{}] ===> ",this.getClass());

        SpelExpressionParser parser = new SpelExpressionParser();

        metaListeners.forEach(metaListener-> {
            EvaluationContext ctx = new StandardEvaluationContext();
//            final String exchange = parser.parseExpression(metaListener.getReceiver().exchange()).getValue(ctx).toString();
//            final String route =parser.parseExpression(metaListener.getReceiver().route()).getValue(ctx).toString();
//            final String queue =parser.parseExpression(metaListener.getReceiver().queue()).getValue(ctx).toString();
            final String exchange =metaListener.getReceiver().exchange();
            final String route =metaListener.getReceiver().route();
            final String queue =metaListener.getReceiver().queue();

            log.debug("绑定 ===>队列 {}",metaListener.getReceiver().queue());
            SimpleMessageListenerContainer container =factory.createListenerContainer();
//            container.getActiveConsumerCount()
            container.setConcurrentConsumers(1);
            container.setMaxConcurrentConsumers(5);
            //设置是否重回队列
            container.setDefaultRequeueRejected(false);
            //设置监听外露
            container.setExposeListenerChannel(true);
            container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
            container.addQueueNames(queue);
            container.setAutoDeclare(true);

            container.setMessageListener(new ChannelAwareMessageListener() {
                @Override
                public void onMessage(Message message, Channel channel) throws Exception {
                    log.debug("message Comming");
                    MsgPojo msgPojo = new MsgPojo();
                    msgPojo.setExchangeName(exchange);
                    msgPojo.setRouteKey(route);
                    msgPojo.setStatus(MsgStatus.RUNNING);
                    /**
                     * @author lucifer 2021-01-13 17:42
                     * @description:
                     * <p>功能: 补充获取 exchange rootkey   </p>
                     */
                    try {
                        msgPojo.setMsgId(message.getMessageProperties().getHeader(HEADER_MESSAGE_POJO_ID_KEY));
                        onMsg(message);
                        channel.basicNack(message.getMessageProperties().getDeliveryTag(),
                                false,false);
//                            MsgPojo  oldPojo = (MsgPojo) redisTemplateDefault.get(RedisType.Default).opsForValue().get(msgPojo.toRedisKey(keyPrefix));
                        log.debug("处理消息[{}] 成功",msgPojo.getMsgId());
                        msgCache.add(new QueueCacheObj(msgPojo));
//                        boolean flag = redisTemplateDefault.get(RedisType.Default).delete(msgPojo.toRedisKey(keyPrefix));
//                        if(!flag){
//                            log.error("[From Redis]清理持久化消息失败 key pattern=[{}]",msgPojo.toRedisKey(keyPrefix));
//                        }
                    } catch (IOException e) {
                        log.debug("消息队列中无{}消息，或已被确认==>{}",
                                message.getMessageProperties().getDeliveryTag(),e.getMessage());
                    } catch (Throwable throwable) {
                        log.debug("消息处理失败，不处理该消息==>{}",throwable);
                    }
                }

            });
            log.debug("绑定 [queue={}]===>[exchange={}] with [{}]", queue,exchange,route);

            containers.add(container);
        });
        return containers.get(0);
    }

    @PostConstruct
    public void init(){

        executorService = Executors.newSingleThreadScheduledExecutor( new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("com.eco.paas.rabbitmq.MQConsumer.SecurityRecvHeartBeat");
                thread.setDaemon(true);
                return thread;
            }
        });
        executorService.scheduleAtFixedRate(new HeartBeatRecv(3,keyPrefix,redisTemplateDefault.get(RedisType.Default),msgCache),
                1,1, TimeUnit.SECONDS);
        log.debug("初始化确认消息心跳线程池");
    }

    private Collection<SecurityMQReceiver> findListenerAnnotations(AnnotatedElement element) {
        return MergedAnnotations.from(element, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
                .stream(SecurityMQReceiver.class)
                .map(ann -> ann.synthesize())
                .collect(Collectors.toList());
    }

    class MetaListener{

        SecurityMQReceiver receiver;
        Method method;

        public MetaListener(SecurityMQReceiver receiver, Method method) {
            this.receiver = receiver;
            this.method = method;
        }

        public SecurityMQReceiver getReceiver() {
            return receiver;
        }

        public void setReceiver(SecurityMQReceiver receiver) {
            this.receiver = receiver;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }
    }

    private static class HeartBeatRecv implements  Runnable{

        Logger log  = LoggerFactory.getLogger(HeartBeatRecv.class);
        int reCountTimes;
        private String redisKeyPattern;
        private RedisTemplate<String,Object> redisTemplate;
        private ConcurrentLinkedQueue<QueueCacheObj> queue;

        HeartBeatRecv(int times,String redisKeyPattern,RedisTemplate<String,Object> redisTemplate ,ConcurrentLinkedQueue<QueueCacheObj> queue){
            this.redisKeyPattern = redisKeyPattern;
            this.redisTemplate = redisTemplate;
            this.queue = queue;
            reCountTimes =times ;
        }

        @Override
        public void run() {
            if(queue.peek()== null){
                log.debug("消息确认缓存为空");
                return;
            }
            log.debug(queue.peek().msgPojo.toString());
            long count = 0;
            //count++
            try{
                while(doRelease(queue.peek())){
                    count ++;
                }
                log.debug("本次释放 {} 个",count);
            }catch (Exception e){
                log.error("{}",e);
            }

        }

        private boolean  doRelease(QueueCacheObj cacheObj){
            boolean isDelSuccess = false;
            if(cacheObj==null){
                return isDelSuccess;
            }
            String redisKey = cacheObj.msgPojo.toRedisKey(redisKeyPattern);
//            cacheObj.reCount++;
//            if(cacheObj.reCount>reCountTimes){
//                queue.poll()
//            }
            if(redisTemplate.hasKey(redisKey)){
                log.debug("find key ===> {}",redisKey);
                isDelSuccess =redisTemplate.delete(redisKey);
                if(isDelSuccess){
                    log.debug("[From Redis]成功清理消息===>{}",redisKey);
                    queue.poll();
                }else{
                    log.error("[From Redis]清理持久化消息失败 key pattern=[{}]",redisKey);
                }
            }else{
                log.error("当前消息[{}]，未找到或还未持久化到Redis中",redisKey);
            }
            log.debug("Queue Size ===>[{}]",queue.size());
            return isDelSuccess;
        }
    }

    private class QueueCacheObj{
        private MsgPojo msgPojo;
        private Date startTime;
        private int reCount;

        public QueueCacheObj(MsgPojo msgPojo) {
            this.msgPojo = msgPojo;
            startTime = new Date();
            reCount = 0;
        }

        public long incr(){
            return ++reCount;
        }

        public MsgPojo getMsgPojo() {
            return msgPojo;
        }

        public void setMsgPojo(MsgPojo msgPojo) {
            this.msgPojo = msgPojo;
        }

        public Date getStartTime() {
            return startTime;
        }

        public void setStartTime(Date startTime) {
            this.startTime = startTime;
        }
    }



}
