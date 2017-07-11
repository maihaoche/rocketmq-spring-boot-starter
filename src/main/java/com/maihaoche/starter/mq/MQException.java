package com.maihaoche.starter.mq;

/**
 * Created by yipin on 2017/6/28.
 * RocketMQ的自定义异常
 */
public class MQException extends RuntimeException {
    public MQException(String msg) {
        super(msg);
    }
}
