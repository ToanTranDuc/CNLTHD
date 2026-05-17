# ========================================
# DOCKERFILE - Notification Service
# Using pre-built JAR (build locally first)
# ========================================

# ========================================
# STAGE: RUNTIME - Chạy ứng dụng
# ========================================
# Sử dụng JRE thay vì JDK để giảm kích thước image
FROM eclipse-temurin:17-jre

WORKDIR /app

# Metadata
LABEL maintainer="HD Bank"
LABEL description="Notification Service - Microservices Architecture"
LABEL version="1.0.0"

# Copy từ build folder (chạy mvn clean package trước)
COPY target/notification-service-1.0.0.jar app.jar

# Expose port
EXPOSE 8083

# Health check
# Định kỳ kiểm tra sức khỏe ứng dụng (sử dụng Spring Actuator)
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8083/api/v1/health || exit 1

# Environment variables (có thể override khi run container)
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=default

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

