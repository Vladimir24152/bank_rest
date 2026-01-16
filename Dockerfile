FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Установите wait-for-it для проверки доступности БД
RUN apk add --no-cache bash
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Можно добавить скрипт ожидания БД, если нужно
ENTRYPOINT ["java", "-jar", "app.jar"]