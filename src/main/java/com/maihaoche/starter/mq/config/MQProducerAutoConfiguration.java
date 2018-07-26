package com.maihaoche.starter.mq.config;

import com.maihaoche.starter.mq.annotation.MQProducer;
import com.maihaoche.starter.mq.annotation.MQTransactionProducer;
import com.maihaoche.starter.mq.base.AbstractMQTransactionProducer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by yipin on 2017/6/29.
 * 自动装配消息生产者
 */
@Slf4j
@Configuration
@ConditionalOnBean(MQBaseAutoConfiguration.class)
public class MQProducerAutoConfiguration extends MQBaseAutoConfiguration {

    @Setter
    private static DefaultMQProducer producer;

    @Bean
    public DefaultMQProducer exposeProducer() throws Exception {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(MQProducer.class);
        //对于仅仅只存在消息消费者的项目，无需构建生产者
        if(CollectionUtils.isEmpty(beans)){
            return null;
        }
        if(producer == null) {
            Assert.notNull(mqProperties.getProducerGroup(), "producer group must be defined");
            Assert.notNull(mqProperties.getNameServerAddress(), "name server address must be defined");
            producer = new DefaultMQProducer(mqProperties.getProducerGroup());
            producer.setNamesrvAddr(mqProperties.getNameServerAddress());
            producer.setSendMsgTimeout(mqProperties.getSendMsgTimeout());
            producer.setSendMessageWithVIPChannel(mqProperties.getVipChannelEnabled());
            producer.start();
        }
        return producer;
    }

    @PostConstruct
    public void configTransactionProducer() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(MQTransactionProducer.class);
        if(CollectionUtils.isEmpty(beans)){
            return;
        }
        ExecutorService executorService = new ThreadPoolExecutor(beans.size(), beans.size()*2, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("client-transaction-msg-check-thread");
                return thread;
            }
        });
        Environment environment = applicationContext.getEnvironment();
        beans.entrySet().forEach( transactionProducer -> {
            try {
                AbstractMQTransactionProducer beanObj = AbstractMQTransactionProducer.class.cast(transactionProducer.getValue());
                MQTransactionProducer anno = beanObj.getClass().getAnnotation(MQTransactionProducer.class);

                TransactionMQProducer producer = new TransactionMQProducer(environment.resolvePlaceholders(anno.producerGroup()));
                producer.setNamesrvAddr(mqProperties.getNameServerAddress());
                producer.setExecutorService(executorService);
                producer.setTransactionListener(beanObj);
                producer.start();
                beanObj.setProducer(producer);
            } catch (Exception e) {
                log.error("build transaction producer error : {}", e);
            }
        });
    }
}
