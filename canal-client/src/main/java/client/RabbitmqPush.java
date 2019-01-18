package client;


import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * RabbitmqPush
 */

@Component
public class RabbitmqPush {
    @Autowired
    private AmqpTemplate rabbitTemplate;

    @Value("${rabbitmq.queuename}")
    String queueName;

	public void push_mq(String[] argv) {
        MessageProperties mProperties = new MessageProperties();
        mProperties.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
        for (String arg : argv) {
            Message message = new Message(arg.getBytes(), mProperties);
            rabbitTemplate.send(queueName, message);
        }
	}

}