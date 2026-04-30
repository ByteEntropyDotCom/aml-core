package com.byteentropy.aml_core.model;

public record Alert(String transactionId, String reason, String severity) {}