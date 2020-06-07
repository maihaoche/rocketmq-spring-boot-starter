package com.maihaoche.starter.mq.config;

import com.maihaoche.starter.mq.annotation.MQConsumer;
import com.maihaoche.starter.mq.base.AbstractMQPushConsumer;
import com.maihaoche.starter.mq.base.MessageExtConst;
import org.junit.After;
import org.junit.Test;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MQConsumerAutoConfigurationTest {

    private AnnotationConfigApplicationContext context;

    private void prepareApplicationContext() {
        this.context = new AnnotationConfigApplicationContext();
        TestPropertyValues.of("spring.rocketmq.name-server-address=127.0.0.1:9876").applyTo(this.context.getEnvironment());
        this.context.register(TestConsumer.class);
        this.context.register(MQConsumerAutoConfiguration.class);
        this.context.refresh();
    }

    private void prepareApplicationContextCMOrderly() {
        this.context = new AnnotationConfigApplicationContext();
        TestPropertyValues.of("spring.rocketmq.name-server-address=127.0.0.1:9876").applyTo(this.context.getEnvironment());
        this.context.register(TestConsumerOrderly.class);
        this.context.register(MQConsumerAutoConfiguration.class);
        this.context.refresh();
    }

    private void prepareApplicationContextCMError() {
        this.context = new AnnotationConfigApplicationContext();
        TestPropertyValues.of("spring.rocketmq.name-server-address=127.0.0.1:9876").applyTo(this.context.getEnvironment());
        this.context.register(TestConsumerErrorCM.class);
        this.context.register(MQConsumerAutoConfiguration.class);
        this.context.refresh();
    }

    private void prepareApplicationContextMissingNS() {
        this.context = new AnnotationConfigApplicationContext();
        this.context.register(TestConsumer.class);
        this.context.register(MQConsumerAutoConfiguration.class);
        this.context.refresh();
    }

    private void prepareApplicationContextMissingParent() {
        this.context = new AnnotationConfigApplicationContext();
        TestPropertyValues.of("spring.rocketmq.name-server-address=127.0.0.1:9876").applyTo(this.context.getEnvironment());
        this.context.register(TestConsumerMissingParent.class);
        this.context.register(MQConsumerAutoConfiguration.class);
        this.context.refresh();
    }

    @After
    public void close() {
        this.context.close();
    }

    @Test
    public void testConsumerConfiguration() throws Exception {
        prepareApplicationContext();
        TestConsumer testConsumer = context.getBean(TestConsumer.class);
        assertNotNull(testConsumer.getConsumer());
        assertEquals(testConsumer.getConsumer().getConsumerGroup(), "test_consumer_group");
        assertEquals(testConsumer.getConsumer().getNamesrvAddr(), "127.0.0.1:9876");
    }

    @Test(expected = RuntimeException.class)
    public void testConsumerMissingNS() {
        prepareApplicationContextMissingNS();
    }

    @Test(expected = RuntimeException.class)
    public void testConsumerMissingParent() {
        prepareApplicationContextMissingParent();
    }

    @Test
    public void testConsumerModeOrderly() {
        prepareApplicationContextCMOrderly();
        TestConsumerOrderly testConsumer = context.getBean(TestConsumerOrderly.class);
        assertNotNull(testConsumer.getConsumer());
        assertEquals(testConsumer.getConsumer().getConsumerGroup(), "test_consumer_group");
        assertEquals(testConsumer.getConsumer().getNamesrvAddr(), "127.0.0.1:9876");
    }

    @Test(expected = RuntimeException.class)
    public void testConsumerModeERROR() {
        prepareApplicationContextCMError();
    }

    @Component
    @MQConsumer(consumerGroup = "test_consumer_group", topic = "test_topic")
    static class TestConsumer extends AbstractMQPushConsumer<String> {
        @Override
        public boolean process(String message, Map<String, Object> extMap) {
            return true;
        }
    }

    @Component
    @MQConsumer(consumerGroup = "test_consumer_group", topic = "test_topic", consumeMode = MessageExtConst.CONSUME_MODE_ORDERLY)
    static class TestConsumerOrderly extends AbstractMQPushConsumer<String> {
        @Override
        public boolean process(String message, Map<String, Object> extMap) {
            return true;
        }
    }

    @Component
    @MQConsumer(consumerGroup = "test_consumer_group", topic = "test_topic", consumeMode = "TYPE_MESSAGE_MODE")
    static class TestConsumerErrorCM extends AbstractMQPushConsumer<String> {
        @Override
        public boolean process(String message, Map<String, Object> extMap) {
            return true;
        }
    }

    @Component
    @MQConsumer(consumerGroup = "test_consumer_group", topic = "test_topic")
    static class TestConsumerMissingParent {
    }

}
