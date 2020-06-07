package com.maihaoche.starter.mq.base;

import com.maihaoche.starter.mq.MQException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by pufang on 20180726.
 * RocketMQ的事务生产者的抽象基类
 */
public abstract class AbstractMQTransactionProducer implements TransactionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMQTransactionProducer.class);

    private TransactionMQProducer transactionProducer;

    public void setProducer(TransactionMQProducer transactionProducer) {
        this.transactionProducer = transactionProducer;
    }

    public SendResult sendMessageInTransaction(Message msg, Object arg) throws MQException {
        try {
            SendResult sendResult = transactionProducer.sendMessageInTransaction(msg, arg);
            if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
                LOGGER.error("事务消息发送失败，topic : {}, msgObj {}", msg.getTopic(), msg);
                throw new MQException("事务消息发送失败，topic :" + msg.getTopic() + ", status :" + sendResult.getSendStatus());
            }
            LOGGER.info("发送事务消息成功，事务id: {}", msg.getTransactionId());
            return sendResult;
        } catch (Exception e) {
            LOGGER.error("事务消息发送失败，topic : {}, msgObj {}", msg.getTopic(), msg);
            throw new MQException("事务消息发送失败，topic :" + msg.getTopic() + ",e:" + e.getMessage());
        }
    }


}
