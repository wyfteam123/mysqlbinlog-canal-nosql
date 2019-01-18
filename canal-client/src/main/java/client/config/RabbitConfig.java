package client.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${rabbitmq.queuename}")
    String queueName;

    @Value("${rabbitmq.durable}")
    boolean durable;

    @Bean
    public Queue queue() {
        return new Queue(queueName, durable, false, false, null);
    }

}
