package com.byteentropy.aml_core.engine;

import com.byteentropy.aml_core.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public final class HighValueRule implements Rule {
    private static final Logger log = LoggerFactory.getLogger(HighValueRule.class);

    @Value("${aml.rules.high-value.limit}")
    private BigDecimal limit;

    @Override
    public boolean isSuspicious(Transaction current) {
        boolean triggered = current.amount().compareTo(limit) > 0;
        
        if (triggered) {
            log.debug("HighValueRule check: Transaction {} exceeded limit {}", 
                      current.transactionId(), limit);
        }
        
        return triggered;
    }

    @Override
    public String getRuleName() {
        return "HIGH_VALUE_TRANSACTION";
    }
}