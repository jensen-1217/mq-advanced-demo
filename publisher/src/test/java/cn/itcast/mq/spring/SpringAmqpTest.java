package cn.itcast.mq.spring;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringAmqpTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSendMessage2SimpleQueue() throws InterruptedException {
        String routingKey = "simple";
        String message = "hello, spring amqp!";
        rabbitTemplate.convertAndSend("simple.direct", routingKey, message);
    }

    @Test
    public void testSendMessage1SimpleQueue() throws InterruptedException {
        String routingKey = "simple";
        String message = "hello, spring amqp!";
        // 自定义数据 设置非持久化消息
        Message message1 = MessageBuilder.withBody(message.getBytes(StandardCharsets.UTF_8)).setDeliveryMode(MessageDeliveryMode.PERSISTENT).build();
//        CorrelationData data = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend("simple.direct",routingKey,message1);
//        // 发送消息
//        rabbitTemplate.convertAndSend("simple.direct", routingKey, message, new MessagePostProcessor() {
//            // 后置处理消息
//            @Override
//            public Message postProcessMessage(Message message) throws AmqpException {
//                // 设置消息的持久化方式
//                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT);
//                return message;
//            }
//        },data);
    }

    @Test
    public void sendTTLQueue() throws InterruptedException {
        String routingKey = "ttl";
        String message = "hello, spring amqp!";
        // 自定义数据 设置非持久化消息
        Message build = MessageBuilder.withBody(message.getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .setExpiration("5000")//设置消息失效时间
                .build();
        rabbitTemplate.convertAndSend("ttl.direct",routingKey,build);
        log.info("当前系统时间：{}", LocalDateTime.now());
    }

    @Test
    public void sendDelayQueue() throws InterruptedException {
        String routingKey = "delay";
        String message = "hello, spring amqp!";
        // 自定义数据 设置非持久化消息
        Message build = MessageBuilder.withBody(message.getBytes(StandardCharsets.UTF_8))
                .setHeader("x-delay",10000)
                .build();
        rabbitTemplate.convertAndSend("delay.direct",routingKey,build);
        log.info("当前系统时间：{}", LocalDateTime.now());
    }

    @Test
    public void testSendManyMsg(){

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 300000; i++) {
            CorrelationData data = new CorrelationData(UUID.randomUUID().toString());
            rabbitTemplate.convertAndSend( "","lazy.queue", "message "+i,data);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("批量发送消息 消耗时间: " + (endTime - startTime));
    }
}
