package com.maihaoche.starter.mq.base;

import com.google.gson.Gson;
import com.maihaoche.starter.mq.MQException;
import com.maihaoche.starter.mq.annotation.MQKey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.Message;

import javax.annotation.PreDestroy;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

/**
 * Created by yipin on 2017/6/27.
 * RocketMQ的生产者的抽象基类
 */
@Slf4j
public abstract class AbstractMQProducer {

    private static Gson gson = new Gson();

    private MessageQueueSelector messageQueueSelector = new SelectMessageQueueByHash();

    public AbstractMQProducer() {
    }

    private String tag;

    /**
     * 重写此方法,或者通过setter方法注入tag设置producer bean 级别的tag
     *
     * @return tag
     */
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Setter
    @Getter
    private DefaultMQProducer producer;

    private String topic;

    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * 重写此方法定义bean级别的topic，如果有返回有效topic，可以直接使用 sendMessage() 方法发送消息
     *
     * @return topic
     */
    public String getTopic() {
        return this.topic;
    }

    private Message genMessage(String topic, String tag, Object msgObj) {
        String messageKey= "";
        try {
            Field[] fields = msgObj.getClass().getDeclaredFields();
            for (Field field : fields) {
                Annotation[] allFAnnos= field.getAnnotations();
                if(allFAnnos.length > 0) {
                    for (int i = 0; i < allFAnnos.length; i++) {
                        if(allFAnnos[i].annotationType().equals(MQKey.class)) {
                            field.setAccessible(true);
                            MQKey mqKey = MQKey.class.cast(allFAnnos[i]);
                            messageKey = StringUtils.isEmpty(mqKey.prefix()) ? field.get(msgObj).toString() : (mqKey.prefix() + field.get(msgObj).toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("parse key error : {}" , e.getMessage());
        }
        String str = gson.toJson(msgObj);
        if(StringUtils.isEmpty(topic)) {
            if(StringUtils.isEmpty(getTopic())) {
                throw new RuntimeException("no topic defined to send this message");
            }
            topic = getTopic();
        }
        Message message = new Message(topic, str.getBytes(Charset.forName("utf-8")));
        if (!StringUtils.isEmpty(tag)) {
            message.setTags(tag);
        } else if (!StringUtils.isEmpty(getTag())) {
            message.setTags(getTag());
        }
        if(StringUtils.isNotEmpty(messageKey)) {
            message.setKeys(messageKey);
        }
        return message;
    }


    /**
     * fire and forget 不关心消息是否送达，可以提高发送tps
     *
     * @param topic topic
     * @param tag tag
     * @param msgObj 消息体
     * @throws MQException 消息异常
     */
    public void sendOneWay(String topic, String tag, Object msgObj) throws MQException {
        try {
            if(null == msgObj) {
                return;
            }
            producer.sendOneway(genMessage(topic, tag, msgObj));
            log.info("send onway message success : {}", msgObj);
        } catch (Exception e) {
            log.error("消息发送失败，topic : {}, e {}", topic, e);
            throw new MQException("消息发送失败，topic :" + topic + ",e:" + e);
        }
    }

    /**
     * fire and forget 不关心消息是否送达，可以提高发送tps
     *
     * @param msgObj 消息体
     * @throws MQException 消息异常
     */
    public void sendOneWay(Object msgObj) throws MQException {
        sendOneWay("", "", msgObj);
    }

    /**
     * fire and forget 不关心消息是否送达，可以提高发送tps
     *
     * @param tag tag
     * @param msgObj 消息体
     * @throws MQException 消息异常
     */
    public void sendOneWay(String tag, Object msgObj) throws MQException {
        sendOneWay("", tag, msgObj);
    }


    /**
     * 可以保证同一个queue有序
     *
     * @param topic topic
     * @param tag tag
     * @param msgObj 消息体
     * @param hashKey 用于hash后选择queue的key
     */
    public void sendOneWayOrderly(String topic, String tag, Object msgObj, String hashKey) {
        if(null == msgObj) {
            return;
        }
        if(StringUtils.isEmpty(hashKey)) {
            // fall back to normal
            sendOneWay(topic, tag, msgObj);
        }
        try {
            producer.sendOneway(genMessage(topic, tag, msgObj), messageQueueSelector, hashKey);
            log.info("send onway message orderly success : {}", msgObj);
        } catch (Exception e) {
            log.error("消息发送失败，topic : {}, msgObj {}", topic, msgObj);
            throw new MQException("顺序消息发送失败，topic :" + topic + ",e:" + e.getMessage());
        }
    }

    /**
     * 同步发送消息
     * @param topic  topic
     * @param tag tag
     * @param msgObj  消息体
     * @throws MQException 消息异常
     */
    public void syncSend(String topic, String tag, Object msgObj) throws MQException {
        try {
            if(null == msgObj) {
                return;
            }
            SendResult sendResult = producer.send(genMessage(topic, tag, msgObj));
            log.info("send rocketmq message ,messageId : {}", sendResult.getMsgId());
            this.doAfterSyncSend(sendResult);
        } catch (Exception e) {
            log.error("消息发送失败，topic : {}, msgObj {}", topic, msgObj);
            throw new MQException("消息发送失败，topic :" + topic + ",e:" + e.getMessage());
        }
    }

    /**
     * 同步发送消息
     * @param msgObj  消息体
     * @throws MQException 消息异常
     */
    public void syncSend(Object msgObj) throws MQException {
        syncSend("", "", msgObj);
    }

    /**
     * 同步发送消息
     * @param tag  消息tag
     * @param msgObj  消息体
     * @throws MQException 消息异常
     */
    public void syncSend(String tag, Object msgObj) throws MQException {
        syncSend("", tag, msgObj);
    }

    /**
     * 同步发送消息
     * @param topic  topic
     * @param tag tag
     * @param msgObj  消息体
     * @param hashKey  用于hash后选择queue的key
     * @throws MQException 消息异常
     */
    public void syncSendOrderly(String topic, String tag, Object msgObj, String hashKey) throws MQException {
        if(null == msgObj) {
            return;
        }
        if(StringUtils.isEmpty(hashKey)) {
            // fall back to normal
            syncSend(topic, tag, msgObj);
        }
        try {
            SendResult sendResult = producer.send(genMessage(topic, tag, msgObj), messageQueueSelector, hashKey);
            log.info("send rocketmq message orderly ,messageId : {}", sendResult.getMsgId());
            this.doAfterSyncSend(sendResult);
        } catch (Exception e) {
            log.error("顺序消息发送失败，topic : {}, msgObj {}", topic, msgObj);
            throw new MQException("顺序消息发送失败，topic :" + topic + ",e:" + e.getMessage());
        }
    }

    /**
     * 异步发送消息带tag
     * @param topic topic
     * @param tag tag
     * @param msgObj msgObj
     * @param sendCallback 回调
     * @throws MQException 消息异常
     */
    public void asyncSend(String topic, String tag, Object msgObj, SendCallback sendCallback) throws MQException {
        try {
            if (null == msgObj) {
                return;
            }
            producer.send(genMessage(topic, tag, msgObj), sendCallback);
            log.info("send rocketmq message async");
        } catch (Exception e) {
            log.error("消息发送失败，topic : {}, msgObj {}", topic, msgObj);
            throw new MQException("消息发送失败，topic :" + topic + ",e:" + e.getMessage());
        }
    }

    /**
     * 异步发送消息不带tag和topic
     * @param msgObj msgObj
     * @param sendCallback 回调
     * @throws MQException 消息异常
     */
    public void asyncSend(Object msgObj, SendCallback sendCallback) throws MQException {
        asyncSend("", "", msgObj, sendCallback);
    }

    /**
     * 异步发送消息不带tag和topic
     * @param tag msgtag
     * @param msgObj msgObj
     * @param sendCallback 回调
     * @throws MQException 消息异常
     */
    public void asyncSend(String tag, Object msgObj, SendCallback sendCallback) throws MQException {
        asyncSend("", tag, msgObj, sendCallback);
    }

    /**
     * 异步发送消息带tag
     * @param topic topic
     * @param tag tag
     * @param msgObj msgObj
     * @param sendCallback 回调
     * @param hashKey 用于hash后选择queue的key
     * @throws MQException 消息异常
     */
    public void asyncSend(String topic, String tag, Object msgObj, SendCallback sendCallback, String hashKey) throws MQException {
        if (null == msgObj) {
            return;
        }
        if(StringUtils.isEmpty(hashKey)) {
            // fall back to normal
            asyncSend(topic, tag, msgObj, sendCallback);
        }
        try {
            producer.send(genMessage(topic, tag, msgObj), messageQueueSelector, hashKey, sendCallback);
            log.info("send rocketmq message async");
        } catch (Exception e) {
            log.error("消息发送失败，topic : {}, msgObj {}", topic, msgObj);
            throw new MQException("消息发送失败，topic :" + topic + ",e:" + e.getMessage());
        }
    }

    /**
     * 重写此方法处理发送后的逻辑
     *
     * @param sendResult  发送结果
     */
    public void doAfterSyncSend(SendResult sendResult) {}
}