package com.byteentropy.aml_core.service;

import com.byteentropy.aml_core.engine.Rule;
import com.byteentropy.aml_core.model.Alert;
import com.byteentropy.aml_core.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class AmlService {
    private static final Logger log = LoggerFactory.getLogger(AmlService.class);
    private final List<Rule> rules;

    public AmlService(List<Rule> rules) {
        this.rules = rules;
    }

    @Async // Runs this logic on a separate Virtual Thread
    public CompletableFuture<Optional<Alert>> analyzeTransaction(Transaction tx) {
        log.info("Async Analysis Started for TX: {}", tx.transactionId());
        
        for (Rule rule : rules) {
            if (rule.isSuspicious(tx)) {
                Alert alert = new Alert(tx.transactionId(), rule.getRuleName(), "HIGH_RISK");
                log.warn("ALERT TRIGGERED: {}", alert);
                return CompletableFuture.completedFuture(Optional.of(alert));
            }
        }
        
        return CompletableFuture.completedFuture(Optional.empty());
    }
}