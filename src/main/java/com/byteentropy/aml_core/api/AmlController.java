package com.byteentropy.aml_core.api;

import com.byteentropy.aml_core.model.Transaction;
import com.byteentropy.aml_core.service.AmlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/aml")
public class AmlController {
    private final AmlService amlService;

    public AmlController(AmlService amlService) {
        this.amlService = amlService;
    }

    @PostMapping("/check")
    public CompletableFuture<ResponseEntity<?>> check(@RequestBody Transaction transaction) {
        // We return the future directly. 
        // Spring handles the async wait without blocking the Tomcat thread.
        return amlService.analyzeTransaction(transaction)
                .thenApply(result -> result
                        .<ResponseEntity<?>>map(alert -> ResponseEntity.status(403).body(alert))
                        .orElse(ResponseEntity.ok("Transaction Clear")));
    }
}