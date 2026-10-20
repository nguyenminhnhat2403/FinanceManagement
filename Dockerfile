# Giai đoạn 1: dùng Maven và Java để build ứng dụng
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package

# Giai đoạn 2: chuẩn bị môi trường chạy
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/FinanceManagement-0.0.1-SNAPSHOT.jar application.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=60.0", "-jar", "/app/application.jar"]