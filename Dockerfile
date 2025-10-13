# Multi-stage build optimized for Render
FROM node:18-alpine AS frontend-build

WORKDIR /app/react-app
COPY react-app/package*.json ./
RUN npm ci --only=production
COPY react-app/ ./
RUN npm run build

FROM maven:3.9.4-openjdk-21-slim AS backend-build

WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline -B

COPY src ./src
COPY --from=frontend-build /app/react-app/build ./src/main/resources/static
RUN mvn clean package -DskipTests -B

FROM openjdk:21-jre-slim

WORKDIR /app
COPY --from=backend-build /app/target/naics-demo-*.jar app.jar

# Use PORT environment variable for Render
EXPOSE $PORT
CMD ["sh", "-c", "java -Dserver.port=$PORT -jar app.jar"]