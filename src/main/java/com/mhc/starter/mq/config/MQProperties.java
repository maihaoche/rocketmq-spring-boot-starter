package com.mhc.starter.mq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by yipin on 2017/6/28.
 * RocketMQ的配置参数
 */
@Data
@ConfigurationProperties(prefix = "rocketmq")
public class MQProperties {
    private String nameServerAddress;
    private String producerGroup;
}
