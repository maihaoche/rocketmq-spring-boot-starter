package com.maihaoche.starter.mq.base;

import com.google.gson.Gson;
import com.maihaoche.starter.mq.MQException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import javax.annotation.PreDestroy;
import java.nio.charset.Charset;

/**
 * Created by yipin on 2017/6/27.
 * RocketMQ的生产者的抽象基类
 */
@Slf4j
public abstract class AbstractMQProducer {

    private static Gson gson = new Gson();

    public AbstractMQProducer() {
    }

    @Setter
    private DefaultMQProducer producer;

    @PreDestroy
    public void destroyProducer() {
        if (producer != null) {
            synchronized (AbstractMQProducer.class) {
                if (producer != null) {
                    producer.shutdown();
                }
            }
        }
    }

    /**
     * 重写此方法定义bean级别的topic，如果有返回有效topic，可以直接使用 sendMessage() 方法发送消息
     *
     * @return
     */
    public String getTopic() {
        return "";
    }

    /**
     * 同步发送消息
     * @param topic  topic
     * @param tag tag
     * @param msgObj  消息体
     * @throws MQException
     */
    public void synSend(String topic, String tag, Object msgObj) throws MQException {
        try {
            if(StringUtils.isEmpty(topic) || null == msgObj) {
                return;
            }
            String str = gson.toJson(msgObj);
            Message message;
            if (!StringUtils.isEmpty(tag)) {
                message = new Message(topic, tag, str.getBytes(Charset.forName("utf-8")));
            } else {
                message = new Message(topic, str.getBytes(Charset.forName("utf-8")));
            }
            SendResult sendResult = producer.send(message);
            log.info("send rocketmq message ,messageId : {}", sendResult.getMsgId());
            this.doAfterSynSend(sendResult);
        } catch (Exception e) {
            log.error("消息发送失败，topic : {}, msgObj {}", topic, msgObj);
            throw new MQException("消息发送失败，topic :" + topic + ",e:" + e.getMessage());
        }
    }

    /**
     * 同步发送消息，不带tag
     *
     * @param topic topic
     * @param msgObj 消息体
     * @throws MQException
     */
    public void synSend(String topic, Object msgObj) throws MQException {
        try {
            if(StringUtils.isEmpty(topic) || null == msgObj) {
                return;
            }
            String str = gson.toJson(msgObj);
            SendResult sendResult = producer.send(new Message(topic, str.getBytes(Charset.forName("utf-8"))));
            log.info("send rocketmq message ,messageId : {}", sendResult.getMsgId());
            this.doAfterSynSend(sendResult);
        } catch (Exception e) {
            log.error("消息发送失败，topic : {}, msgObj {}", topic, msgObj);
            throw new MQException("消息发送失败，topic :" + topic + ",e:" + e.getMessage());
        }
    }

    /**
     * 异步发送消息
     * @param topic topic
     * @param msgObj msgObj
     * @param sendCallback 回调
     * @throws MQException
     */
    public void asynSend(String topic, Object msgObj, SendCallback sendCallback) throws MQException {
        try {
            if (StringUtils.isEmpty(topic) || null == msgObj) {
                return;
            }
            String str = gson.toJson(msgObj);
            producer.send(new Message(topic, str.getBytes(Charset.forName("utf-8"))), sendCallback);
            log.info("send rocketmq message asyn");
        } catch (Exception e) {
            log.error("消息发送失败，topic : {}, msgObj {}", topic, msgObj);
            throw new MQException("消息发送失败，topic :" + topic + ",e:" + e.getMessage());
        }
    }

    /**
     * 兼容buick中的方式
     *
     * @param msgObj
     * @throws MQException
     */
    public void sendMessage(Object msgObj) throws MQException {
        if(StringUtils.isEmpty(getTopic())) {
            throw new MQException("如果用这种方式发送消息，请在实例中重写 getTopic() 方法返回需要发送的topic");
        }
        synSend(getTopic(), msgObj);
    }

    /**
     * 重写此方法处理发送后的逻辑
     *
     * @param sendResult  发送结果
     */
    public void doAfterSynSend(SendResult sendResult) {
    }
}
