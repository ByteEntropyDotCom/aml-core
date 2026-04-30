package com.byteentropy.aml_core.engine;

import com.byteentropy.aml_core.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public final class VelocityRule implements Rule {
    private static final Logger log = LoggerFactory.getLogger(VelocityRule.class);
    private final StringRedisTemplate redisTemplate;

    @Value("${aml.rules.velocity.threshold}")
    private int threshold;

    @Value("${aml.rules.velocity.window-seconds}")
    private int windowSeconds;

    // LUA Script: Atomically increments and sets expiry only on the first hit
    private static final String LUA_SCRIPT = 
        "local current = redis.call('INCR', KEYS[1]); " +
        "if current == 1 then " +
        "  redis.call('EXPIRE', KEYS[1], ARGV[1]); " +
        "end; " +
        "return current;";

    public VelocityRule(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

@Override
    public boolean isSuspicious(Transaction current) {
        try {
            String key = "aml:velocity:" + current.userId();
            
            Long count = redisTemplate.execute(
                new DefaultRedisScript<>(LUA_SCRIPT, Long.class),
                Collections.singletonList(key),
                String.valueOf(windowSeconds)
            );
            
            log.info("Velocity check | User: {} | Count: {} | Threshold: {}", current.userId(), count, threshold);
            return count != null && count > threshold;
        } catch (Exception e) {
            // CRITICAL: Change this line to see the REAL error
            log.error("CRITICAL REDIS ERROR for user {}: ", current.userId(), e); 
            return false; 
        }
    }

    @Override
    public String getRuleName() { return "VELOCITY_STRIKE"; }
}