package com.byteentropy.aml_core.model;
import java.math.BigDecimal;
import java.time.Instant;

public record Transaction(String transactionId, String userId, BigDecimal amount, Instant timestamp) {}