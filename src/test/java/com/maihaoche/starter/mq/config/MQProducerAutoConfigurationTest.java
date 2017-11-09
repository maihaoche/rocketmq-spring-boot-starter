package com.maihaoche.starter.mq.config;

import com.maihaoche.starter.mq.annotation.MQProducer;
import com.maihaoche.starter.mq.base.AbstractMQProducer;
import com.maihaoche.starter.mq.base.AbstractMQPushConsumer;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.junit.After;
import org.junit.Test;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class MQProducerAutoConfigurationTest {

    private AnnotationConfigApplicationContext context;

    private void prepareApplicationContext() {
        this.context = new AnnotationConfigApplicationContext();
        EnvironmentTestUtils.addEnvironment(this.context, "rocketmq.name-server-address:127.0.0.1:9876");
        EnvironmentTestUtils.addEnvironment(this.context, "rocketmq.producer-group:test-producer-group");
        this.context.register(TestProducer.class);
        this.context.register(MQProducerAutoConfiguration.class);
        this.context.refresh();
    }

    @After
    public void close() {
        this.context.close();
    }

    @Test
    public void testProducerConfiguration() throws Exception {
        prepareApplicationContext();
        DefaultMQProducer dp = context.getBean(DefaultMQProducer.class);
        assertNotNull(dp);
        assertEquals(dp.getProducerGroup(), "test-producer-group");
        assertEquals(dp.getNamesrvAddr(), "127.0.0.1:9876");
    }

    @Component
    @MQProducer
    static class TestProducer extends AbstractMQProducer {
    }

}
