# rocketmq-spring-boot-starter
spring boot starter for RocketMQ 
首先添加maven依赖：

```
		<dependency>
			<groupId>com.mhc</groupId>
			<artifactId>rocketmq-spring-boot-starter</artifactId>
			<version>0.0.1-SNAPSHOT</version>
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

创建发送方：

```
@MQProducer
class DemoProducer : AbstractMQProducer() {
}
```

创建消费方：

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