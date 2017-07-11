# rocketmq-spring-boot-starter

spring boot starter for RocketMQ 

首先添加maven依赖：

```
<dependency>
    <groupId>com.maihaoche</groupId>
    <artifactId>spring-boot-starter-rocketmq</artifactId>
    <version>0.0.1</version>
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
demoProducer.synSend("suclogger", "test message")
    
```

