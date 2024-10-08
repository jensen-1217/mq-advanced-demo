package cn.itcast.mq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CommonConfig implements ApplicationContextAware {
    /**
     *
     * @param applicationContext IOC容器对象
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 获取RabbitTemplate
        RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);
        // 设置ConfirmCallback
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * @param correlationData  自定义的数据
             * @param ack  是否确认
             * @param cause  原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                if(ack){
                    // 3.1.ack，消息成功
                    log.debug("消息发送成功");
                }else{
                    // 3.2.nack，消息失败
                    log.error("消息发送失败,  原因{}", cause);
                }
            }
        });
        // 设置ReturnsCallback
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returned) {
                //判断是不是延迟队列
                Long receivedDelayLong = returned.getMessage().getMessageProperties().getReceivedDelayLong();
                if (receivedDelayLong<=0){
                    // 投递失败，记录日志
                    log.error("消息发送失败，应答码:{}，原因:{}，交换机:{}，路由键:{},消息:{}",
                            returned.getReplyCode(),returned.getReplyText(),returned.getExchange(),returned.getRoutingKey(),returned.getMessage());
                    // 如果有业务需要，可以重发消息
                }

            }
        });
    }
    @Bean
    public DirectExchange simpleExchange(){
        // 三个参数：交换机名称、是否持久化、当没有queue与其绑定时是否自动删除
        return new DirectExchange("simple.direct", true, false);
    }
//    @Bean
//    public Queue simpleQueue(){
//        return new Queue("simple.queue",true);
//    }
    @Bean
    public Queue simpleQueue(){
        return QueueBuilder.durable("simple.queue") // 指定队列名称，并持久化
                .deadLetterExchange("dl.direct")// 指定死信交换机
                .deadLetterRoutingKey("dl")//指定死信路由
                .build();
    }
    @Bean
    public Binding binding(){
        return BindingBuilder.bind(simpleQueue()).to(simpleExchange()).with("simple");
    }


    // 声明死信交换机 dl.direct
    @Bean
    public DirectExchange dlExchange(){
        return new DirectExchange("dl.direct", true, false);
    }
    // 声明存储死信的队列 dl.queue
    @Bean
    public Queue dlQueue(){
        return new Queue("dl.queue", true);
    }
    // 将死信队列 与 死信交换机绑定
    @Bean
    public Binding dlBinding(){
        return BindingBuilder.bind(dlQueue()).to(dlExchange()).with("dl");
    }

    // 惰性队列
    @Bean
    public Queue lazyQueue(){
        return QueueBuilder.durable("lazy.queue")
                .lazy()
                .build();
    }
    // 普通队列
    @Bean
    public Queue normalQueue(){
        return QueueBuilder.durable("normal.queue")
                .build();
    }
}
