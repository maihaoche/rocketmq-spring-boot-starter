package com.maihaoche.starter.mq.config;

import com.maihaoche.starter.mq.annotation.MQConsumer;
import com.maihaoche.starter.mq.base.AbstractMQPushConsumer;
import com.maihaoche.starter.mq.base.MessageExtConst;
import com.maihaoche.starter.mq.trace.common.OnsTraceConstants;
import com.maihaoche.starter.mq.trace.dispatch.AsyncAppender;
import com.maihaoche.starter.mq.trace.dispatch.impl.AsyncTraceAppender;
import com.maihaoche.starter.mq.trace.dispatch.impl.AsyncTraceDispatcher;
import com.maihaoche.starter.mq.trace.tracehook.OnsConsumeMessageHookImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by suclogger on 2017/6/28.
 * 自动装配消息消费者
 */
@Slf4j
@Configuration
@ConditionalOnBean(MQBaseAutoConfiguration.class)
public class MQConsumerAutoConfiguration extends MQBaseAutoConfiguration {

    private AsyncTraceDispatcher asyncTraceDispatcher;

    @PostConstruct
    public void init() throws Exception {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(MQConsumer.class);
        if(!CollectionUtils.isEmpty(beans) && mqProperties.getTraceEnabled()) {
            initAsyncAppender();
        }
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            publishConsumer(entry.getKey(), entry.getValue());
        }
    }

    private AsyncTraceDispatcher initAsyncAppender() {
        if(asyncTraceDispatcher != null) {
            return asyncTraceDispatcher;
        }
        try {
            Properties tempProperties = new Properties();
            tempProperties.put(OnsTraceConstants.MaxMsgSize, "128000");
            tempProperties.put(OnsTraceConstants.AsyncBufferSize, "2048");
            tempProperties.put(OnsTraceConstants.MaxBatchNum, "1");
            tempProperties.put(OnsTraceConstants.WakeUpNum, "1");
            tempProperties.put(OnsTraceConstants.NAMESRV_ADDR, mqProperties.getNameServerAddress());
            tempProperties.put(OnsTraceConstants.InstanceName, UUID.randomUUID().toString());
            AsyncTraceAppender asyncAppender = new AsyncTraceAppender(tempProperties);
            asyncTraceDispatcher = new AsyncTraceDispatcher(tempProperties);
            asyncTraceDispatcher.start(asyncAppender, "DEFAULT_WORKER_NAME");
        } catch (MQClientException e) {
            e.printStackTrace();
        }
        return asyncTraceDispatcher;
    }

    private void publishConsumer(String beanName, Object bean) throws Exception {
        MQConsumer mqConsumer = applicationContext.findAnnotationOnBean(beanName, MQConsumer.class);
        if (StringUtils.isEmpty(mqProperties.getNameServerAddress())) {
            throw new RuntimeException("name server address must be defined");
        }
        Assert.notNull(mqConsumer.consumerGroup(), "consumer's consumerGroup must be defined");
        Assert.notNull(mqConsumer.topic(), "consumer's topic must be defined");
        if (!AbstractMQPushConsumer.class.isAssignableFrom(bean.getClass())) {
            throw new RuntimeException(bean.getClass().getName() + " - consumer未实现Consumer抽象类");
        }

        String consumerGroup = applicationContext.getEnvironment().getProperty(mqConsumer.consumerGroup());
        if (StringUtils.isEmpty(consumerGroup)) {
            consumerGroup = mqConsumer.consumerGroup();
        }
        String topic = applicationContext.getEnvironment().getProperty(mqConsumer.topic());
        if (StringUtils.isEmpty(topic)) {
            topic = mqConsumer.topic();
        }

        // 配置push consumer
        if (AbstractMQPushConsumer.class.isAssignableFrom(bean.getClass())) {
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);
            consumer.setNamesrvAddr(mqProperties.getNameServerAddress());
            consumer.setMessageModel(MessageModel.valueOf(mqConsumer.messageMode()));
            consumer.subscribe(topic, StringUtils.join(mqConsumer.tag(), "||"));
            consumer.setInstanceName(UUID.randomUUID().toString());
            consumer.setVipChannelEnabled(mqProperties.getVipChannelEnabled());
            AbstractMQPushConsumer abstractMQPushConsumer = (AbstractMQPushConsumer) bean;
            if (MessageExtConst.CONSUME_MODE_CONCURRENTLY.equals(mqConsumer.consumeMode())) {
                consumer.registerMessageListener((List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) ->
                        abstractMQPushConsumer.dealMessage(list, consumeConcurrentlyContext));
            } else if (MessageExtConst.CONSUME_MODE_ORDERLY.equals(mqConsumer.consumeMode())) {
                consumer.registerMessageListener((List<MessageExt> list, ConsumeOrderlyContext consumeOrderlyContext) ->
                        abstractMQPushConsumer.dealMessage(list, consumeOrderlyContext));
            } else {
                throw new RuntimeException("unknown consume mode ! only support CONCURRENTLY and ORDERLY");
            }
            abstractMQPushConsumer.setConsumer(consumer);

            // 为Consumer增加消息轨迹回发模块
            if (mqProperties.getTraceEnabled()) {
                try {
                    consumer.getDefaultMQPushConsumerImpl().registerConsumeMessageHook(
                            new OnsConsumeMessageHookImpl(asyncTraceDispatcher));
                } catch (Throwable e) {
                    log.error("system mqtrace hook init failed ,maybe can't send msg trace data");
                }
            }

            consumer.start();
        }

        log.info(String.format("%s is ready to subscribe message", bean.getClass().getName()));
    }

}