package com.byteentropy.aml_core;

import com.byteentropy.aml_core.model.Alert;
import com.byteentropy.aml_core.model.Transaction;
import com.byteentropy.aml_core.service.AmlService;
import redis.embedded.RedisServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test") // This triggers the @Profile("!test") logic to skip the Cloud Config
@TestPropertySource(properties = {
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379",
    "spring.data.redis.password=",
    "spring.data.redis.ssl.enabled=false"
})
class AmlServiceTest {

    @Autowired
    private AmlService amlService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static RedisServer redisServer;
    private static boolean redisAvailable = false;

    @BeforeAll
    static void startRedis() {
        try {
            redisServer = new RedisServer(6379);
            redisServer.start();
            redisAvailable = true;
        } catch (Exception e) {
            System.err.println("Redis setup failed: " + e.getMessage());
        }
    }

    @AfterAll
    static void stopRedis() {
        try {
            if (redisServer != null) redisServer.stop();
        } catch (Exception e) {
             System.err.println("Redis setup failed: " + e.getMessage());
        }
        
    }

    @BeforeEach
    void clean() {
        if (redisAvailable) redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    @DisplayName("Async Velocity Test: Should handle background processing")
    void testAsyncVelocity() throws Exception {
        Assumptions.assumeTrue(redisAvailable);
        String userId = "async-user-" + UUID.randomUUID().toString().substring(0,5);

        // Send 10 transactions
        for (int i = 0; i < 10; i++) {
            amlService.analyzeTransaction(new Transaction("tx-"+i, userId, BigDecimal.TEN, Instant.now())).get();
        }

        // The 11th one should return an alert
        CompletableFuture<Optional<Alert>> future = amlService.analyzeTransaction(
            new Transaction("tx-11", userId, BigDecimal.TEN, Instant.now())
        );

        Optional<Alert> result = future.get(); // Wait for the "Conveyor Belt" to finish
        assertTrue(result.isPresent(), "Should alert on 11th transaction");
        assertEquals("VELOCITY_STRIKE", result.get().reason());
    }

    @Test
    @DisplayName("Async HV Test: Should alert immediately on large amount")
    void testAsyncHighValue() throws Exception {
        Transaction tx = new Transaction("hv-1", "user-1", new BigDecimal("20000"), Instant.now());
        
        Optional<Alert> result = amlService.analyzeTransaction(tx).get();
        
        assertTrue(result.isPresent());
        assertEquals("HIGH_VALUE_TRANSACTION", result.get().reason());
    }
}