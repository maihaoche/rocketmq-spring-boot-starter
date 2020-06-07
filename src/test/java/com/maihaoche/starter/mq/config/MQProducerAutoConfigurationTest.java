package com.maihaoche.starter.mq.config;

import com.maihaoche.starter.mq.annotation.MQProducer;
import com.maihaoche.starter.mq.base.AbstractMQProducer;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.junit.After;
import org.junit.Test;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import static org.junit.Assert.*;

public class MQProducerAutoConfigurationTest {

    private AnnotationConfigApplicationContext context;

    private void prepareApplicationContextEmpty() {
        this.context = new AnnotationConfigApplicationContext();
        this.context.register(MQBaseAutoConfiguration.class, MQProducerAutoConfiguration.class);
        this.context.refresh();
    }

    private void prepareApplicationContextMissingConfigure() {
        this.context = new AnnotationConfigApplicationContext();
        this.context.register(TestProducer.class);
        MQProducerAutoConfiguration.setProducer(null);
        this.context.register(MQProducerAutoConfiguration.class);
        this.context.refresh();
    }

    private void prepareApplicationContextMissingProducerGroupConfigure() {
        this.context = new AnnotationConfigApplicationContext();
        TestPropertyValues.of("spring.rocketmq.name-server-address=127.0.0.1:9876").applyTo(this.context.getEnvironment());
        this.context.register(TestProducer.class);
        MQProducerAutoConfiguration.setProducer(null);
        this.context.register(MQProducerAutoConfiguration.class);
        this.context.refresh();
    }

    private void prepareApplicationContextWithoutParent() {
        this.context = new AnnotationConfigApplicationContext();
        TestPropertyValues.of("spring.rocketmq.name-server-address=127.0.0.1:9876")
                .and("rocketmq.producer-group=test-producer-group")
                .applyTo(this.context.getEnvironment());
        this.context.register(TestProducerNoParent.class);
        this.context.register(MQProducerAutoConfiguration.class);
        this.context.refresh();
    }

    private void prepareApplicationContext() {
        this.context = new AnnotationConfigApplicationContext();
        TestPropertyValues.of("spring.rocketmq.name-server-address=127.0.0.1:9876", "spring.rocketmq.producer-group=test-producer-group")
                .applyTo(this.context.getEnvironment());
        this.context.register(TestProducer.class);
        this.context.register(MQBaseAutoConfiguration.class, MQProducerAutoConfiguration.class);
        this.context.refresh();
    }

    @After
    public void close() {
        this.context.close();
    }


    @Test
    public void testEmpty() {
        prepareApplicationContextEmpty();
        assertTrue(context.getBeansOfType(DefaultMQProducer.class).isEmpty());
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
        TestProducer dp = context.getBean(TestProducer.class);
        assertNotNull(dp);
        assertEquals(dp.getProducer().getProducerGroup(), "test-producer-group");
        assertEquals(dp.getProducer().getNamesrvAddr(), "127.0.0.1:9876");
    }


    @Component
    @MQProducer
    static class TestProducer extends AbstractMQProducer {
    }

    @Component
    @MQProducer
    static class TestProducerNoParent {
    }


}
