## AML Core Engine
AML Core is a high-performance Anti-Money Laundering (AML) transaction monitoring service built with Java 21 and Spring Boot 3.4. It leverages Virtual Threads (Project Loom) to handle high-throughput transaction streams with minimal resource overhead, utilizing Redis for low-latency stateful rule evaluation.

--- 

## 🚀 Key Features (Updated)

### 1. Fully Asynchronous Pipeline: 
Utilizing CompletableFuture and @Async, the engine decouples transaction intake from analysis. This ensures the API remains responsive (sub-5ms) even under heavy rule-processing loads.

### 2. Java 21 Virtual Threads: 
Engineered for massive concurrency. By enabling spring.threads.virtual.enabled, the service handles background tasks using lightweight threads, preventing thread-exhaustion during traffic spikes.

### 3. Atomic Velocity Evaluation: 
Uses Redis Lua Scripts to ensure that "Increment + Expiry" operations are atomic, preventing race conditions in distributed environments.

---

## 🛠 Tech Stack

| Technology     | Purpose                                                      |
|----------------|--------------------------------------------------------------|
| Java 21        | Core language utilizing Virtual Threads                      |
| Spring Async   | Non-blocking execution for the transaction conveyor belt     |
| Redis          | In-memory data store for tracking user velocity              |
| JUnit 5        | Integration and unit testing framework                       |
| Maven          | Dependency management and build automation                   |


---

## ⚙️ Configuration

Business rules are decoupled from the code , a dedicated Task Executor to manage the asynchronous background pool and can be adjusted via src/main/resources/application.properties:

```Properties
# Velocity Rule: Flag if user sends > 10 transfers in 120 seconds
aml.rules.velocity.threshold=10
aml.rules.velocity.window-seconds=120

# High Value Rule: Flag any transaction over $10,000.00
aml.rules.high-value.limit=10000.00

# Background Worker Pool Configuration
spring.task.execution.pool.core-size=8
spring.task.execution.pool.max-size=50
spring.task.execution.pool.queue-capacity=10000
```

Note on Redis SSL: If using managed services (like Redis Cloud), ensure the useSsl() configuration in RedisConfig.java matches your endpoint's requirements. Use nc -zv <host> <port> to verify connectivity, but check application logs for NotSslRecordException to diagnose protocol mismatches.

---

### 🧪 Testing
The test suite has been hardened to handle Asynchronous Assertions.

```
1. Non-Blocking Verification: 
Tests now utilize .get() on futures to validate background processing results.

2. Architecture-Aware Redis: 
Uses codemonstur/embedded-redis for native support across different OS architectures (Intel/ARM).
```

 Run Tests

```Bash
mvn clean test
```

----

## 🚀 Quick Start

### 1. Prerequisites

```Java 21 & Maven 3.9+
Docker (for running Redis)
```

### 2. Launch Infrastructure

Start the Redis instance using the provided docker-compose:

```Bash
docker-compose up -d
```

### 3. Run the Application

```Bash
mvn spring-boot:run
```

### 4. Test with a High-Value Transaction

Open your terminal and send a $15,000 transaction. The system will respond with a 403 Forbidden and an AML Alert:

```Bash
curl -X POST http://localhost:8080/api/v1/aml/check \
-H "Content-Type: application/json" \
-d '{
  "transactionId": "tx-999",
  "userId": "user_01",
  "amount": 15000.00,
  "timestamp": "'$(date -u +%Y-%m-%dT%H:%M:%SZ)'"
}'
```

### 5. Test Velocity (The "Smurfing" Check)

Run this loop to send 11 quick transactions. The 11th one will trigger a VELOCITY_STRIKE:

```Bash
for i in {1..11}; do 
  curl -s -X POST http://localhost:8080/api/v1/aml/check \
  -H "Content-Type: application/json" \
  -d "{\"transactionId\":\"v-$i\",\"userId\":\"smurf_01\",\"amount\":10.00,\"timestamp\":\"$(date -u +%Y-%m-%dT%H:%M:%SZ)\"}"
done
```
* Note: Because analysis is asynchronous, check the application logs to see the VELOCITY_STRIKE alerts triggered in the background

---

### Coverage Summary:

1. HV-1/2: Validates High-Value alerts and precise boundary conditions.

2. VEL-1/2: Validates Redis-backed velocity strikes and "Happy Path" safe user behavior.

3. GEN-1/2: Tests engine resilience against edge cases like zero-amount transactions.

## 📂 Project Structure
```Plaintext
src/main/java/com/byteentropy/aml_core
├── api         # REST Endpoints (Non-blocking Controllers)
├── config      # Async & Thread Pool Configurations
├── engine      # Rule definitions (Lua scripts, Velocity, HighValue)
├── model       # Domain models (Transaction, Alert)
├── service     # Core AmlService logic (CompletableFuture)
└── AmlCoreApp  # Main entry point (@EnableAsync)
```

--- 

## 📝 License
This project is licensed under the MIT License - see the LICENSE file for details.