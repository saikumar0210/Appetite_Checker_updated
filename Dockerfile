# Multi-stage build for React + Spring Boot
FROM node:18-alpine AS frontend-build

WORKDIR /app/react-app
COPY react-app/package*.json ./
RUN npm ci
COPY react-app/ ./
RUN npm run build

FROM openjdk:21-jdk-slim AS backend-build

WORKDIR /app
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src ./src
COPY --from=frontend-build /app/react-app/build ./src/main/resources/static
RUN ./mvnw clean package -DskipTests

FROM openjdk:21-jre-slim

WORKDIR /app
COPY --from=backend-build /app/target/naics-demo-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]