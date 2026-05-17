# Notification Service

Notification Service là một component trong hệ thống Microservices, chịu trách nhiệm xử lý các thông báo từ các service khác thông qua Apache Kafka.

## Tính năng

- **Event-Driven Architecture**: Sử dụng Kafka để nhận events từ các service khác
- **Order Notification**: Xử lý thông báo khi đơn hàng được tạo thành công
- **Service Discovery**: Tích hợp Eureka Discovery Server
- **Monitoring**: Tích hợp Actuator, Prometheus và Zipkin
- **Distributed Tracing**: Hỗ trợ tracing qua Zipkin/Brave

## Công nghệ sử dụng

- **Spring Boot 3.2.0**
- **Java 17**
- **Spring Kafka**: Xử lý Kafka messaging
- **Spring Cloud Eureka**: Service discovery
- **Spring Boot Actuator**: Monitoring endpoints
- **Micrometer Prometheus**: Metrics collection
- **Micrometer Tracing (Brave)**: Distributed tracing
- **Lombok**: Code generation
- **Docker**: Containerization

## Cấu trúc dự án

```
notification-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com.hdbank.notificationservice/
│   │   │   │   ├── controller/       # REST Controllers
│   │   │   │   │   └── HealthController.java
│   │   │   │   ├── listener/         # Kafka Consumers
│   │   │   │   │   └── NotificationListener.java
│   │   │   │   ├── service/          # Business Logic
│   │   │   │   │   └── NotificationService.java
│   │   │   │   ├── dto/              # Data Transfer Objects
│   │   │   │   │   └── OrderEvent.java
│   │   │   │   └── NotificationServiceApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── banner.txt
│   └── test/
│       └── java/
│           └── com.hdbank.notificationservice/
│               └── NotificationServiceApplicationTests.java
├── pom.xml
└── README.md
```

## Cấu hình

### application.properties

Cấu hình chính:
- **Spring Application Name**: `notification-service`
- **Server Port**: `8083`
- **Kafka Bootstrap Servers**: `localhost:9092`
- **Kafka Topic**: `notificationTopic`
- **Eureka Server**: `http://localhost:8761/eureka`

## Chạy ứng dụng

### Điều kiện tiên quyết

1. **Java 17** hoặc cao hơn
2. **Maven 3.6+**
3. **Kafka** running on `localhost:9092`
4. **Eureka Server** running on `localhost:8761`

### Build

```bash
mvn clean package
```

### Run

```bash
mvn spring-boot:run
```

Hoặc chạy jar file:

```bash
java -jar target/notification-service-1.0.0.jar
```

## API Endpoints

### Health Check
- **GET** `/api/v1/health` - Health status
- **GET** `/api/v1/health/detailed` - Detailed health information

### Actuator Endpoints
- **GET** `/actuator/health` - Spring Health check
- **GET** `/actuator/prometheus` - Prometheus metrics
- **GET** `/actuator/info` - Application info

## Kafka Topics

### Consumer Topics

- **notificationTopic**: Nhận order events từ order-service
  - Message format:
    ```json
    {
      "orderNumber": "ORD123",
      "message": "Order Placed Successfully",
      "customerId": "CUST001",
      "totalAmount": 150.50,
      "timestamp": 1234567890
    }
    ```

## Monitoring

### Prometheus Metrics

Truy cập: `http://localhost:8083/actuator/prometheus`

### Health Endpoints

- Liveness: `http://localhost:8083/actuator/health/liveness`
- Readiness: `http://localhost:8083/actuator/health/readiness`

## Testing

### Chạy Unit Tests

```bash
mvn test
```

### Test Coverage

Tests bao gồm:
- Application context loading
- Health check endpoints
- Kafka consumer functionality
- Message processing

## Docker

### Build Docker Image

```bash
docker build -t notification-service:1.0.0 .
```

### Run with Docker Compose

```bash
docker-compose up -d
```

## Phần mở rộng

- [ ] Thêm Retry logic cho failed messages
- [ ] Thêm Dead Letter Topic
- [ ] Thêm Email notification thực tế (JavaMailSender)
- [ ] Database persistence
- [ ] Kubernetes deployment manifests
- [ ] Advanced error handling

## Tài liệu tham khảo

- [Spring Kafka Documentation](https://spring.io/projects/spring-kafka)
- [Spring Cloud Netflix Eureka](https://spring.io/projects/spring-cloud-netflix)
- [Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
- [Micrometer Documentation](https://micrometer.io/)

## License

This project is licensed under the MIT License.
