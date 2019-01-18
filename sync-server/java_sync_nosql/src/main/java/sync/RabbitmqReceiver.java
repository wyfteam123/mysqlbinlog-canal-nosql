package sync;

import com.alibaba.fastjson.JSONObject;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import redis.clients.jedis.Jedis;
import sync.model.Binlog;

@Component
@RabbitListener(queues = "${rabbitmq.queuename}")
public class RabbitmqReceiver {

    @Autowired
    Jedis jedis;

    @RabbitHandler
    public void process(String binlogStr) {
        System.out.println("Receiver : " + binlogStr);
        try {
            Binlog binlog = JSONObject.parseObject(binlogStr, Binlog.class);
            if(!StringUtils.isEmpty(binlog)&&!StringUtils.isEmpty(binlog.getEventType())){
                String key = String.format("%s_%s", binlog.getDb(),binlog.getTable());
                if (("DELETE").equals(binlog.getEventType())) {
                    jedis.lrem(key, 0, binlog.getAfter());
                } else if (("INSERT").equals(binlog.getEventType())) {
                    jedis.lpush(key, binlog.getAfter());
                } else {
                    jedis.lrem(key, 0, binlog.getBefore());
                    jedis.lpush(key, binlog.getAfter());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //TODO: handle exception
        }
    }

}
