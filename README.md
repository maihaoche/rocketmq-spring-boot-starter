# rocketmq-spring-boot-starter

spring boot starter for RocketMQ 

<p><a href="http://search.maven.org/#search%7Cga%7C1%7Ccom.maihaoche"><img src="https://maven-badges.herokuapp.com/maven-central/com.maihaoche/spring-boot-starter-rocketmq/badge.svg" alt="Maven Central" style="max-width:100%;"></a><a href="https://github.com/maihaoche/rocketmq-spring-boot-starter/releases"><img src="https://camo.githubusercontent.com/795f06dcbec8d5adcfadc1eb7a8ac9c7d5007fce/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f72656c656173652d646f776e6c6f61642d6f72616e67652e737667" alt="GitHub release" data-canonical-src="https://img.shields.io/badge/release-download-orange.svg" style="max-width:100%;"></a>

首先添加maven依赖：

```
<dependency>
    <groupId>com.maihaoche</groupId>
    <artifactId>spring-boot-starter-rocketmq</artifactId>
    <version>0.0.4</version>
</dependency>
```

添加配置：

```
rocketmq:
  name-server-address: 172.21.10.111:9876
  producer-group: local_pufang_producer
```

在springboot应用主入口添加`@EnableMQConfiguration`注解开启自动装配：

```
@SpringBootApplication
@EnableMQConfiguration
class CamaroDemoApplication {
}
```

定义消息体，通过`@MQKey`注解将对应字段设置为消息key，可以在注解中通过`prefix`定义key的前缀：

```
data class DemoMessage(
        @MQKey(prefix = "sku_")
        val skuId:Long,
        val skuType:String)

```

创建发送方(详见[wiki](https://github.com/maihaoche/rocketmq-spring-boot-starter/wiki/%E6%9C%80%E4%BD%B3%E5%AE%9E%E8%B7%B5-Provider))：

```
@MQProducer
class DemoProducer : AbstractMQProducer() {
}
```

创建消费方(详见[wiki](https://github.com/maihaoche/rocketmq-spring-boot-starter/wiki/%E6%9C%80%E4%BD%B3%E5%AE%9E%E8%B7%B5-Consumer))：

```
@MQConsumer(consumerGroup = "local_pufang_test_consumer", topic = "suclogger")
class DemoConsumer : AbstractMQPushConsumer<DemoMessage>() {
    override fun process(message:DemoMessage) : Boolean {
        return true
    }

}
```

发送消息：

```

// 注入发送者
@Autowired
lateinit var demoProducer:DemoProducer
    
...
    
// 发送
demoProducer.syncSend("suclogger", DemoMessage(1, "plain_message"))
    
```

