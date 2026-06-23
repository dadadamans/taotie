FROM maven:3.8-eclipse-temurin-11 AS builder
WORKDIR /build
COPY pom.xml .
COPY taotie-common taotie-common
COPY taotie-pojo taotie-pojo
COPY taotie-server taotie-server
COPY taotie-gateway taotie-gateway
RUN mvn clean package -pl taotie-gateway -am -DskipTests -q

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=builder /build/taotie-gateway/target/*.jar app.jar
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar"]
