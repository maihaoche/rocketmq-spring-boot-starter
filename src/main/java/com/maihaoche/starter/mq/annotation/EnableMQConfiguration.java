package com.maihaoche.starter.mq.annotation;

import java.lang.annotation.*;

/**
 * Created by suclogger on 2017/6/30.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface EnableMQConfiguration {
}
