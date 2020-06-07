package com.maihaoche.starter.mq.config;

import com.maihaoche.starter.mq.annotation.MQProducer;
import com.maihaoche.starter.mq.annotation.MQTransactionProducer;
import com.maihaoche.starter.mq.base.AbstractMQProducer;
import com.maihaoche.starter.mq.base.AbstractMQTransactionProducer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by yipin on 2017/6/29.
 * 自动装配消息生产者
 */
@Configuration
@ConditionalOnBean(MQBaseAutoConfiguration.class)
public class MQProducerAutoConfiguration extends MQBaseAutoConfiguration implements InitializingBean {

    private final static Logger LOGGER = LoggerFactory.getLogger(MQProducerAutoConfiguration.class);

    private static DefaultMQProducer producer;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.configCommonProducer();
        this.configTransactionProducer();
    }

    private void configCommonProducer() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(MQProducer.class);
        //对于仅仅只存在消息消费者的项目，无需构建生产者
        if (CollectionUtils.isEmpty(beans)) {
            return;
        }
        if (producer == null) {
            Assert.notNull(mqProperties.getProducerGroup(), "producer group must be defined");
            Assert.notNull(mqProperties.getNameServerAddress(), "name server address must be defined");
            producer = new DefaultMQProducer(mqProperties.getProducerGroup());
            producer.setNamesrvAddr(mqProperties.getNameServerAddress());
            producer.setSendMsgTimeout(mqProperties.getSendMsgTimeout());
            producer.setSendMessageWithVIPChannel(mqProperties.getVipChannelEnabled());
            try {
                producer.start();
            } catch (MQClientException e) {
                LOGGER.error("build producer error : {}", e.getLocalizedMessage());
                throw new RuntimeException(e);            }
        }

        beans.forEach((key, value) -> {
            AbstractMQProducer beanObj = (AbstractMQProducer) value;
            beanObj.setProducer(producer);
        });
    }

    private void configTransactionProducer() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(MQTransactionProducer.class);
        if (CollectionUtils.isEmpty(beans)) {
            return;
        }
        ExecutorService executorService = new ThreadPoolExecutor(beans.size(), beans.size() * 2, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2000), r -> {
            Thread thread = new Thread(r);
            thread.setName("client-transaction-msg-check-thread");
            return thread;
        });
        Environment environment = applicationContext.getEnvironment();
        beans.forEach((key, value) -> {
            try {
                AbstractMQTransactionProducer beanObj = (AbstractMQTransactionProducer) value;
                MQTransactionProducer anno = beanObj.getClass().getAnnotation(MQTransactionProducer.class);

                TransactionMQProducer producer = new TransactionMQProducer(environment.resolvePlaceholders(anno.producerGroup()));
                producer.setNamesrvAddr(mqProperties.getNameServerAddress());
                producer.setExecutorService(executorService);
                producer.setTransactionListener(beanObj);
                producer.start();
                beanObj.setProducer(producer);
            } catch (Exception e) {
                LOGGER.error("build transaction producer error : {}", e.getLocalizedMessage());
                throw new RuntimeException(e);
            }
        });
    }

    public static void setProducer(DefaultMQProducer producer) {
        MQProducerAutoConfiguration.producer = producer;
    }
}
