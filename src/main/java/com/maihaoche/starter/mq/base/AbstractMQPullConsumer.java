package com.maihaoche.starter.mq.base;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

/**
 * Created by yipin on 2017/6/27.
 * RocketMQ的消费者(Push模式)处理消息的接口
 */
@Slf4j
public abstract class AbstractMQPullConsumer<T> {

    public AbstractMQPullConsumer() {
    }

    private String topic;

    private DefaultMQPullConsumer consumer;

    public DefaultMQPullConsumer getConsumer() {
        return consumer;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setConsumer(DefaultMQPullConsumer consumer) {
        this.consumer = consumer;
    }

    public void startInner() {
        new Thread(() -> {
            try {
                while(true) {
                    Set<MessageQueue> mqs = consumer.fetchMessageQueuesInBalance(topic);
                    try {
                        for (MessageQueue mq : mqs) {
                            SINGLE_MQ:
                            while (true) {
                                try {//阻塞的拉去消息，中止时间默认20s
                                    long offset = consumer.fetchConsumeOffset(mq, false);
                                    offset = offset < 0 ? 0 : offset;
                                    PullResult pullResult = consumer.pull(mq, null, offset, 10);
                                    switch (pullResult.getPullStatus()) {
                                        case FOUND://pullSataus
                                            dealMessage(pullResult.getMsgFoundList());
                                            break;
                                        case NO_MATCHED_MSG:
                                            break;
                                        case NO_NEW_MSG:
                                            break SINGLE_MQ;
                                        case OFFSET_ILLEGAL:
                                            break;
                                        default:
                                            break;
                                    }
                                    consumer.updateConsumeOffset(mq, pullResult.getNextBeginOffset());
                                } catch (Exception e) {
                                    log.error("consume message fail , e : {}", e);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("start pull consumer fail, e : {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("start pull consumer fail, e : {}", e.getMessage());
            }
        }).start();
    }


    private static Gson gson = new Gson();

    /**
     * 继承这个方法处理消息
     *
     * @param message 消息范型
     */
    public abstract void process(T message);

    /**
     * 原生dealMessage方法，可以重写此方法自定义序列化和返回消费成功的相关逻辑
     *
     * @param list 消息列表
     */
    public void dealMessage(List<MessageExt> list) {
        for(MessageExt messageExt : list) {
            if(messageExt.getReconsumeTimes() != 0) {
                log.info("re-consume times: {}" , messageExt.getReconsumeTimes());
            }
            log.info("receive msgId: {}, tags : {}" , messageExt.getMsgId(), messageExt.getTags());
            T t = parseMessage(messageExt);
            process(t);
        }
    }

    /**
     * 反序列化解析消息
     *
     * @param message  消息体
     * @return 序列化结果
     */
    private T parseMessage(MessageExt message) {
        if (message == null || message.getBody() == null) {
            return null;
        }
        final Type type = this.getMessageType();
        if (type instanceof Class) {
            try {
                Object data = gson.fromJson(new String(message.getBody()), type);
                return (T) data;
            } catch (JsonSyntaxException e) {
                log.error("parse message json fail : {}", e.getMessage());
            }
        } else {
            log.warn("Parse msg error. {}", message);
        }
        return null;
    }

    /**
     * 解析消息类型
     *
     * @return 消息类型
     */
    private Type getMessageType() {
        Type superType = this.getClass().getGenericSuperclass();
        if (superType instanceof ParameterizedType) {
            return ((ParameterizedType) superType).getActualTypeArguments()[0];
        } else {
            // 如果没有定义泛型，解析为Object
            return Object.class;
//            throw new RuntimeException("Unkown parameterized type.");
        }
    }
}
