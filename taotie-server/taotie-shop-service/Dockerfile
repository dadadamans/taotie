 FROM maven:3.8-eclipse-temurin-11 AS builder
WORKDIR /build
COPY pom.xml .
COPY taotie-common taotie-common
COPY taotie-pojo taotie-pojo
COPY taotie-server taotie-server
COPY taotie-shop-service taotie-shop-service
RUN mvn clean package -pl taotie-shop-service -am -DskipTests -q

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=builder /build/taotie-shop-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
