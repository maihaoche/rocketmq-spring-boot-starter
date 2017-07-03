package com.mhc.starter.mq.config;

import com.mhc.starter.mq.annotation.MQProducer;
import com.mhc.starter.mq.base.AbstractMQProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Created by yipin on 2017/6/29.
 * 自动装配消息生产者
 */
@Slf4j
@Configuration
@ConditionalOnBean(MQBaseAutoConfiguration.class)
public class MQProducerAutoConfiguration extends MQBaseAutoConfiguration {

    private DefaultMQProducer producer;

    @PostConstruct
    public void init() throws Exception {
        if(producer == null) {
            if(StringUtils.isEmpty(mqProperties.getProducerGroup())) {
                throw new RuntimeException("请在配置文件中指定消息发送方group！");
            }
            producer = new DefaultMQProducer(mqProperties.getProducerGroup());
            producer.setNamesrvAddr(mqProperties.getNameServerAddress());
            producer.start();
        }
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(MQProducer.class);
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            publishProducer(entry.getKey(), entry.getValue());
        }
    }

    private void publishProducer(String beanName, Object bean) throws Exception {
        if(!AbstractMQProducer.class.isAssignableFrom(bean.getClass())) {
            throw new RuntimeException(beanName + " - producer未继承AbstractMQProducer");
        }
        AbstractMQProducer abstractMQProducer = (AbstractMQProducer) bean;
        abstractMQProducer.setProducer(producer);
        log.info(String.format("%s is ready to produce message", beanName));
    }
}
