package com.maihaoche.starter.mq.base;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.common.message.MessageExt;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by yipin on 2017/6/27.
 * RocketMQ的消费者(Push模式)处理消息的接口
 */
@Slf4j
public abstract class AbstractMQPushConsumer<T> {

    @Getter
    @Setter
    private DefaultMQPushConsumer consumer;

    public AbstractMQPushConsumer() {
    }

    private static Gson gson = new Gson();

    /**
     * 继承这个方法处理消息
     *
     * @param message 消息范型
     * @return 处理结果
     */
    public abstract boolean process(T message);

    /**
     * 原生dealMessage方法，可以重写此方法自定义序列化和返回消费成功的相关逻辑
     *
     * @param list 消息列表
     * @param consumeConcurrentlyContext 上下文
     * @return 消费状态
     */
    public ConsumeConcurrentlyStatus dealMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        for(MessageExt messageExt : list) {
            if(messageExt.getReconsumeTimes() != 0) {
                log.info("re-consume times: {}" , messageExt.getReconsumeTimes());
            }
            log.info("receive msgId: {}, tags : {}" , messageExt.getMsgId(), messageExt.getTags());
            T t = parseMessage(messageExt);
            if( null != t && !process(t)) {
                log.warn("consume fail , ask for re-consume , msgId: {}", messageExt.getMsgId());
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        }
        return  ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    /**
     * 原生dealMessage方法，可以重写此方法自定义序列化和返回消费成功的相关逻辑
     *
     * @param list 消息列表
     * @param consumeOrderlyContext 上下文
     * @return 处理结果
     */
    public ConsumeOrderlyStatus dealMessage(List<MessageExt> list, ConsumeOrderlyContext consumeOrderlyContext) {
        for(MessageExt messageExt : list) {
            if(messageExt.getReconsumeTimes() != 0) {
                log.info("re-consume times: {}" , messageExt.getReconsumeTimes());
            }
            log.info("receive msgId: {}, tags : {}" , messageExt.getMsgId(), messageExt.getTags());
            T t = parseMessage(messageExt);
            if( null != t && !process(t)) {
                log.warn("consume fail , ask for re-consume , msgId: {}", messageExt.getMsgId());
                return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
            }
        }
        return  ConsumeOrderlyStatus.SUCCESS;
    }

    /**
     * 反序列化解析消息
     *
     * @param message  消息体
     * @return 反序列化结果
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
