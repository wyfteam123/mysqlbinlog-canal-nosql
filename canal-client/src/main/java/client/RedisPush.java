package client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;

/**
 * RedisPush
 */
@Component
public class RedisPush {

    @Autowired
    private Jedis jedis;

    @Value("${redis.queuename}")
    String queuename;

    public void push_redis(String[] argv) throws java.io.IOException {
        for (String arg : argv) {
        	jedis.rpush(queuename, arg);
        }

	}
}