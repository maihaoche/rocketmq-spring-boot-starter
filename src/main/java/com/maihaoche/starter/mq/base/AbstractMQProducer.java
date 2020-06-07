package com.maihaoche.starter.mq.base;

import com.maihaoche.starter.mq.MQException;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 * Created by yipin on 2017/6/27.
 * RocketMQ的生产者的抽象基类
 */
public abstract class AbstractMQProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMQProducer.class);

    private final static MessageQueueSelector messageQueueSelector = new SelectMessageQueueByHash();

    public AbstractMQProducer() {
    }

    private DefaultMQProducer producer;

    public void setProducer(DefaultMQProducer producer) {
        this.producer = producer;
    }

    public DefaultMQProducer getProducer() {
        return producer;
    }

    /**
     * 同步发送消息
     *
     * @param message 消息体
     * @throws MQException 消息异常
     */
    public void syncSend(Message message) throws MQException {
        try {
            SendResult sendResult = producer.send(message);
            LOGGER.debug("send rocketmq message ,messageId : {}", sendResult.getMsgId());
            this.doAfterSyncSend(message, sendResult);
        } catch (Exception e) {
            LOGGER.error("消息发送失败，topic : {}, msgObj {}", message.getTopic(), message);
            throw new MQException("消息发送失败，topic :" + message.getTopic() + ",e:" + e.getMessage());
        }
    }


    /**
     * 同步发送消息
     *
     * @param message 消息体
     * @param hashKey 用于hash后选择queue的key
     * @throws MQException 消息异常
     */
    public void syncSendOrderly(Message message, String hashKey) throws MQException {
        if (StringUtils.isEmpty(hashKey)) {
            // fall back to normal
            syncSend(message);
        }
        try {
            SendResult sendResult = producer.send(message, messageQueueSelector, hashKey);
            LOGGER.debug("send rocketmq message orderly ,messageId : {}", sendResult.getMsgId());
            this.doAfterSyncSend(message, sendResult);
        } catch (Exception e) {
            LOGGER.error("顺序消息发送失败，topic : {}, msgObj {}", message.getTopic(), message);
            throw new MQException("顺序消息发送失败，topic :" + message.getTopic() + ",e:" + e.getMessage());
        }
    }

    /**
     * 重写此方法处理发送后的逻辑
     *
     * @param message    发送消息体
     * @param sendResult 发送结果
     */
    public void doAfterSyncSend(Message message, SendResult sendResult) {
    }

    /**
     * 异步发送消息
     *
     * @param message      msgObj
     * @param sendCallback 回调
     * @throws MQException 消息异常
     */
    public void asyncSend(Message message, SendCallback sendCallback) throws MQException {
        try {
            producer.send(message, sendCallback);
            LOGGER.debug("send rocketmq message async");
        } catch (Exception e) {
            LOGGER.error("消息发送失败，topic : {}, msgObj {}", message.getTopic(), message);
            throw new MQException("消息发送失败，topic :" + message.getTopic() + ",e:" + e.getMessage());
        }
    }
}
