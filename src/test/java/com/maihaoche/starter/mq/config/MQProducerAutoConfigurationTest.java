package com.maihaoche.starter.mq.config;

import com.maihaoche.starter.mq.annotation.MQProducer;
import com.maihaoche.starter.mq.base.AbstractMQProducer;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.junit.After;
import org.junit.Test;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MQProducerAutoConfigurationTest {

    private AnnotationConfigApplicationContext context;

    private void prepareApplicationContextEmpty() {
        this.context = new AnnotationConfigApplicationContext();
        this.context.register(MQProducerAutoConfiguration.class);
        this.context.refresh();
    }

    private void prepareApplicationContextMissingConfigure() {
        this.context = new AnnotationConfigApplicationContext();
        this.context.register(TestProducerWithTopicAndTag.class);
        MQProducerAutoConfiguration.setProducer(null);
        this.context.register(MQProducerAutoConfiguration.class);
        this.context.refresh();
    }

    private void prepareApplicationContextMissingProducerGroupConfigure() {
        this.context = new AnnotationConfigApplicationContext();
        EnvironmentTestUtils.addEnvironment(this.context, "rocketmq.name-server-address:127.0.0.1:9876");
        this.context.register(TestProducerWithTopicAndTag.class);
        MQProducerAutoConfiguration.setProducer(null);
        this.context.register(MQProducerAutoConfiguration.class);
        this.context.refresh();
    }

    private void prepareApplicationContextWithoutParent() {
        this.context = new AnnotationConfigApplicationContext();
        EnvironmentTestUtils.addEnvironment(this.context, "rocketmq.name-server-address:127.0.0.1:9876");
        EnvironmentTestUtils.addEnvironment(this.context, "rocketmq.producer-group:test-producer-group");
        this.context.register(TestProducerNoParent.class);
        this.context.register(MQProducerAutoConfiguration.class);
        this.context.refresh();
    }

    private void prepareApplicationContext() {
        this.context = new AnnotationConfigApplicationContext();
        EnvironmentTestUtils.addEnvironment(this.context, "rocketmq.name-server-address:127.0.0.1:9876");
        EnvironmentTestUtils.addEnvironment(this.context, "rocketmq.producer-group:test-producer-group");
        this.context.register(TestProducerWithTopicAndTag.class);
        this.context.register(MQProducerAutoConfiguration.class);
        this.context.refresh();
    }

    private void prepareApplicationContextWithTopicAngTag() {
        this.context = new AnnotationConfigApplicationContext();
        EnvironmentTestUtils.addEnvironment(this.context, "rocketmq.name-server-address:127.0.0.1:9876");
        EnvironmentTestUtils.addEnvironment(this.context, "rocketmq.producer-group:test-producer-group");
        EnvironmentTestUtils.addEnvironment(this.context, "test-topic:test-topic-from-configure");
        EnvironmentTestUtils.addEnvironment(this.context, "test-tag:test-tag-from-configure");
        this.context.register(TestProducerWithTopicAndTag.class);
        this.context.register(MQProducerAutoConfiguration.class);
        this.context.refresh();
    }

    @After
    public void close() {
        this.context.close();
    }


    @Test(expected = RuntimeException.class)
    public void testEmpty() {
        prepareApplicationContextEmpty();
        // throws NoSuchBeanDefinitionException which is subclass of RuntimeException
        context.getBean(DefaultMQProducer.class);
    }

    @Test(expected = RuntimeException.class)
    public void testMissingConfigure() {
        prepareApplicationContextMissingConfigure();
    }

    @Test(expected = RuntimeException.class)
    public void testMissingPGConfigure() {
        prepareApplicationContextMissingProducerGroupConfigure();
    }

    @Test(expected = RuntimeException.class)
    public void testMissingParent() {
        prepareApplicationContextWithoutParent();
    }

    @Test
    public void testProducerConfiguration() throws Exception {
        prepareApplicationContext();
        DefaultMQProducer dp = context.getBean(DefaultMQProducer.class);
        assertNotNull(dp);
        assertEquals(dp.getProducerGroup(), "test-producer-group");
        assertEquals(dp.getNamesrvAddr(), "127.0.0.1:9876");
    }

    @Test
    public void testProducerConfigurationWithTopicAndTag() throws Exception {
        prepareApplicationContext();
        TestProducerWithTopicAndTag producer = context.getBean(TestProducerWithTopicAndTag.class);
        assertNotNull(producer);
        assertEquals(producer.getTopic(), "test-topic");
        assertEquals(producer.getTag(), "test-tag");
    }

    @Test
    public void testProducerConfigurationWithTopicAndTagFromConfigure() throws Exception {
        prepareApplicationContextWithTopicAngTag();
        TestProducerWithTopicAndTag producer = context.getBean(TestProducerWithTopicAndTag.class);
        assertNotNull(producer);
        assertEquals(producer.getTopic(), "test-topic-from-configure");
        assertEquals(producer.getTag(), "test-tag-from-configure");
    }


    @Component
    @MQProducer(topic = "test-topic", tag = "test-tag")
    static class TestProducerWithTopicAndTag extends AbstractMQProducer {
    }

    @Component
    @MQProducer(topic = "test-topic", tag = "test-tag")
    static class TestProducerNoParent{
    }



}
