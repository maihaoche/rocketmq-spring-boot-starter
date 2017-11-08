package com.maihaoche.starter.mq.config;

import com.maihaoche.starter.mq.annotation.MQConsumer;
import com.maihaoche.starter.mq.base.AbstractMQPushConsumer;
import org.junit.After;
import org.junit.Test;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

public class MQConsumerAutoConfigurationTest {

    private AnnotationConfigApplicationContext context;

    private void prepareApplicationContext() {
        this.context = new AnnotationConfigApplicationContext();
        EnvironmentTestUtils.addEnvironment(this.context, "rocketmq.name-server-address:127.0.0.1:9876");
        this.context.register(TestConsumer.class);
        this.context.register(MQConsumerAutoConfiguration.class);
        this.context.refresh();
    }

    @After
    public void close() {
        this.context.close();
    }

    @Test
    public void testDefaultRepositoryConfiguration() throws Exception {
        prepareApplicationContext();
    }

    @Component
    @MQConsumer(consumerGroup = "test_consumer_group", topic = "test_topic")
    static class TestConsumer extends AbstractMQPushConsumer<String> {
        @Override
        public boolean process(String message, Map<String, Object> extMap) {
            return true;
        }
    }

}
