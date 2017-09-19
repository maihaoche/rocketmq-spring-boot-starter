package com.maihaoche.starter.mq.base;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.util.Assert;

/**
 * Comments：RocketMQ消费抽象基类
 * Author：Jay Chang
 * Create Date：2017/9/14
 * Modified By：
 * Modified Date：
 * Why & What is modified：
 * Version：v1.0
 */
@Slf4j
public abstract class AbstractMQConsumer<T> {

    protected static Gson gson = new Gson();

    /**
     * 反序列化解析消息
     *
     * @param message  消息体
     * @return 序列化结果
     */
    protected T parseMessage(MessageExt message) {
        if (message == null || message.getBody() == null) {
            return null;
        }
        final Type type = this.getMessageType();
        if (type instanceof Class) {
            try {
                T data = gson.fromJson(new String(message.getBody()), type);
                return data;
            } catch (JsonSyntaxException e) {
                log.error("parse message json fail : {}", e.getMessage());
            }
        } else {
            log.warn("Parse msg error. {}", message);
        }
        return null;
    }

    /**
     * 解析消息类型
     *
     * @return 消息类型
     */
    protected Type getMessageType() {
        Type superType = this.getClass().getGenericSuperclass();
        if (superType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) superType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            Assert.isTrue(actualTypeArguments.length == 1, "Number of type arguments must be 1");
            return actualTypeArguments[0];
        } else {
            // 如果没有定义泛型，解析为Object
            return Object.class;
//            throw new RuntimeException("Unkown parameterized type.");
        }
    }
}
