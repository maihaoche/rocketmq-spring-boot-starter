package com.maihaoche.starter.mq.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Created by yipin on 2017/6/27.
 * RocketMQ消费者自动装配注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface MQConsumer {
    String consumerGroup();
    String topic();
    /**
     * 广播模式消费： BROADCASTING
     * 集群模式消费： CLUSTERING
     */
    String messageMode() default "CLUSTERING";
    /**
     * 使用线程池并发消费: CONCURRENTLY("CONCURRENTLY"),
     * 单线程消费: ORDERLY("ORDERLY");
     */
    String consumeMode() default "CONCURRENTLY";
    String[] tag() default {"*"};
}
