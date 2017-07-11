package com.maihaoche.starter.mq.config;

import com.maihaoche.starter.mq.annotation.MQConsumer;
import com.maihaoche.starter.mq.base.AbstractMQPushConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * Created by suclogger on 2017/6/28.
 * 自动装配消息消费者
 */
@Slf4j
@Configuration
@ConditionalOnBean(MQBaseAutoConfiguration.class)
public class MQConsumerAutoConfiguration extends MQBaseAutoConfiguration {
    @PostConstruct
    public void init() throws Exception {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(MQConsumer.class);
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            publishConsumer(entry.getKey(), entry.getValue());
        }
    }

    private void publishConsumer(String beanName, Object bean) throws Exception {
        MQConsumer mqConsumer = applicationContext.findAnnotationOnBean(beanName, MQConsumer.class);
        String consumerGroup = applicationContext.getEnvironment().getProperty(mqConsumer.consumerGroup());
        if(StringUtils.isEmpty(consumerGroup)) {
            consumerGroup = mqConsumer.consumerGroup();
        }
        String topic = applicationContext.getEnvironment().getProperty(mqConsumer.topic());
        if(StringUtils.isEmpty(topic)) {
            topic = mqConsumer.topic();
        }
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);
        consumer.setNamesrvAddr(mqProperties.getNameServerAddress());
        consumer.subscribe(topic, mqConsumer.tag());
        consumer.registerMessageListener((List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) -> {
            if(!AbstractMQPushConsumer.class.isAssignableFrom(bean.getClass())) {
                throw new RuntimeException(bean.getClass().getName() + " - consumer未实现IMQPushConsumer接口");
            }
            AbstractMQPushConsumer abstractMQPushConsumer = (AbstractMQPushConsumer) bean;
            return abstractMQPushConsumer.dealMessage(list, consumeConcurrentlyContext);
        });
        consumer.start();
        log.info(String.format("%s is ready to subscribe message", bean.getClass().getName()));
    }
}
