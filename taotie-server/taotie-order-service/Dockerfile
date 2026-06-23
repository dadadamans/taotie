FROM maven:3.8-eclipse-temurin-11 AS builder
WORKDIR /build
COPY pom.xml .
COPY taotie-common taotie-common
COPY taotie-pojo taotie-pojo
COPY taotie-server taotie-server
COPY taotie-order-service taotie-order-service
RUN mvn clean package -pl taotie-order-service -am -DskipTests -q

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=builder /build/taotie-order-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
