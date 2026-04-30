package com.byteentropy.aml_core.engine;

import com.byteentropy.aml_core.model.Transaction;

public sealed interface Rule permits VelocityRule, HighValueRule {
    boolean isSuspicious(Transaction current);
    String getRuleName();
}