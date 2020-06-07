package com.maihaoche.starter.mq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by yipin on 2017/6/28.
 * RocketMQ的配置参数
 */
@ConfigurationProperties(prefix = "spring.rocketmq")
public class MQProperties {
    /**
     * config name server address
     */
    private String nameServerAddress;
    /**
     * config producer group , default to DPG+RANDOM UUID like DPG-fads-3143-123d-1111
     */
    private String producerGroup;
    /**
     * config send message timeout
     */
    private Integer sendMsgTimeout = 3000;
    /**
     * switch of trace message consumer: send message consumer info to topic: rmq_sys_TRACE_DATA
     */
    private Boolean traceEnabled = Boolean.TRUE;

    /**
     * switch of send message with vip channel
     */
    private Boolean vipChannelEnabled = Boolean.TRUE;

    public String getNameServerAddress() {
        return nameServerAddress;
    }

    public void setNameServerAddress(String nameServerAddress) {
        this.nameServerAddress = nameServerAddress;
    }

    public String getProducerGroup() {
        return producerGroup;
    }

    public void setProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
    }

    public Integer getSendMsgTimeout() {
        return sendMsgTimeout;
    }

    public void setSendMsgTimeout(Integer sendMsgTimeout) {
        this.sendMsgTimeout = sendMsgTimeout;
    }

    public Boolean getTraceEnabled() {
        return traceEnabled;
    }

    public void setTraceEnabled(Boolean traceEnabled) {
        this.traceEnabled = traceEnabled;
    }

    public Boolean getVipChannelEnabled() {
        return vipChannelEnabled;
    }

    public void setVipChannelEnabled(Boolean vipChannelEnabled) {
        this.vipChannelEnabled = vipChannelEnabled;
    }
}
