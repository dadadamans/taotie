FROM maven:3.8-eclipse-temurin-11 AS builder
WORKDIR /build
COPY pom.xml .
COPY taotie-common taotie-common
COPY taotie-pojo taotie-pojo
COPY taotie-server taotie-server
COPY taotie-auth-service taotie-auth-service
RUN mvn clean package -pl taotie-auth-service -am -DskipTests -q

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=builder /build/taotie-auth-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
