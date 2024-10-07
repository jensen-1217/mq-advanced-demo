package cn.itcast.mq.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@Slf4j
public class SpringRabbitListener {

    @RabbitListener(queues = "simple.queue")
    public void listenSimpleQueue(String msg, Channel channel, Message message){
//        System.out.println("消费者接收到simple.queue的消息：【" + msg + "】");
        log.info("消费者接收到simple.queue的消息：【{}】{}", msg, LocalDateTime.now());
        // 模拟异常
       if (1==1){
           throw new RuntimeException("消费方法出现异常");
       }
        log.debug("消息处理完成！");
//        try {
//            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);//手动确认ack
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(name = "dl.queue", durable = "true"),
//            exchange = @Exchange(name = "dl.direct"),
//            key = "ttl"
//    ))
//    public void listenDlQueue(String msg){
//        log.info("接收到 dl.queue的延迟消息：{}", msg);
//    }

    @RabbitListener(queues = "dl.queue")
    public void dlQueue(String msg){
        log.info("消费者接收到dl.queue的消息:【{}】,{}",msg,LocalDateTime.now());
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "delay.queue", durable = "true"),
            exchange = @Exchange(name = "delay.direct",delayed = "true"),
            key = "delay"
    ))
    public void listenDelayedQueue(String msg){
        log.info("接收到 delay.queue的延迟消息：{}{}", msg,LocalDateTime.now());
    }
}
