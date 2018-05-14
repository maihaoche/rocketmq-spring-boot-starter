# spring boot starter for RocketMQ [![Build Status](https://travis-ci.org/maihaoche/rocketmq-spring-boot-starter.svg?branch=master)](https://travis-ci.org/maihaoche/rocketmq-spring-boot-starter) [![Coverage Status](https://coveralls.io/repos/github/maihaoche/rocketmq-spring-boot-starter/badge.svg?branch=master)](https://coveralls.io/github/maihaoche/rocketmq-spring-boot-starter?branch=master)

<p><a href="http://search.maven.org/#search%7Cga%7C1%7Ccom.maihaoche"><img src="https://maven-badges.herokuapp.com/maven-central/com.maihaoche/spring-boot-starter-rocketmq/badge.svg" alt="Maven Central" style="max-width:100%;"></a><a href="https://github.com/maihaoche/rocketmq-spring-boot-starter/releases"><img src="https://camo.githubusercontent.com/795f06dcbec8d5adcfadc1eb7a8ac9c7d5007fce/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f72656c656173652d646f776e6c6f61642d6f72616e67652e737667" alt="GitHub release" data-canonical-src="https://img.shields.io/badge/release-download-orange.svg" style="max-width:100%;"></a>


### 项目介绍

[Rocketmq](https://github.com/apache/rocketmq) 是由阿里巴巴团队开发并捐赠给apache团队的优秀消息中间件，承受过历年双十一大促的考验。

你可以通过本项目轻松的集成Rocketmq到你的SpringBoot项目中。
本项目主要包含以下特性

* [x] 同步发送消息
* [x] 异步发送消息
* [x] 广播发送消息
* [x] 有序发送和消费消息
* [x] 发送延时消息
* [x] 消息tag和key支持
* [x] 自动序列化和反序列化消息体
* [x] 消息的实际消费方IP追溯
* [ ] ...
* [x] ~~发送即忘消息~~（可能由于直接抛弃所有异常导致消息静默丢失，弃用）
* [x] ~~拉取方式消费~~（配置方式复杂，位点可能发生偏移，弃用）



### 简单入门实例


##### 1. 添加maven依赖：

```java
<dependency>
    <groupId>com.maihaoche</groupId>
    <artifactId>spring-boot-starter-rocketmq</artifactId>
    <version>0.0.7</version>
</dependency>
```

##### 2. 添加配置：

```java
spring:
    rocketmq:
      name-server-address: 172.21.10.111:9876
      # 可选, 如果无需发送消息则忽略该配置
      producer-group: local_pufang_producer
      # 发送超时配置毫秒数, 可选, 默认3000
      send-msg-timeout: 5000
      # 追溯消息具体消费情况的开关，默认打开
      #trace-enabled: false
      # 是否启用VIP通道，默认打开
      #vip-channel-enabled: false
```
##### 3. 程序入口添加注解开启自动装配

在springboot应用主入口添加`@EnableMQConfiguration`注解开启自动装配：

```java
@SpringBootApplication
@EnableMQConfiguration
class DemoApplication {
}
```

##### 4. 构建消息体

通过我们提供的`Builder`类创建消息对象，详见[wiki](https://github.com/maihaoche/rocketmq-spring-boot-starter/wiki/构建消息体)


```java
MessageBuilder.of(new MSG_POJO()).topic("some-msg-topic").build();
```


##### 5. 创建发送方

详见[wiki](https://github.com/maihaoche/rocketmq-spring-boot-starter/wiki/%E6%9C%80%E4%BD%B3%E5%AE%9E%E8%B7%B5-Provider)：


```java
@MQProducer
public class DemoProducer extends AbstractMQProducer{
}
```

##### 6. 创建消费方

详见[wiki](https://github.com/maihaoche/rocketmq-spring-boot-starter/wiki/%E6%9C%80%E4%BD%B3%E5%AE%9E%E8%B7%B5-Consumer)：
**支持配置项解析**，如存在`suclogger-test-cluster`配置项，会优先将topic解析为配置项对应的值。

```java
@MQConsumer(topic = "suclogger-test-cluster", consumerGroup = "local_sucloger_dev")
public class DemoConsumer extends AbstractMQPushConsumer {

    @Override
    public boolean process(Object message, Map extMap) {
        // extMap 中包含messageExt中的属性和message.properties中的属性
        System.out.println(message);
        return true;
    }
}
```

##### 7. 发送消息：

```java

// 注入发送者
@Autowired
private DemoProducer demoProducer;
    
...
    
// 发送
demoProducer.syncSend(msg)
    
```


