# === Stage 1: Build React frontend ===
FROM node:18-alpine AS frontend-build

WORKDIR /app/react-app

# Copy dependency files
COPY react-app/package*.json ./

# ✅ Install ALL dependencies (include devDependencies)
RUN npm install

# ✅ Add system libraries required for some Node builds
RUN apk add --no-cache python3 make g++ libc6-compat

# Copy source and build
COPY react-app/ ./
RUN npm run build

# === Stage 2: Build Spring Boot backend ===
FROM maven:3.9.4-eclipse-temurin-21 AS backend-build

WORKDIR /app

# Preload dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copy backend source and React build output
COPY src ./src
COPY --from=frontend-build /app/react-app/build ./src/main/resources/static

# Build Spring Boot JAR (skip tests for faster build)
RUN mvn clean package -DskipTests -B

# === Stage 3: Run final app ===
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=backend-build /app/target/naics-demo-*.jar app.jar

# Render automatically provides PORT
EXPOSE $PORT
CMD ["sh", "-c", "java -Dserver.port=$PORT -jar app.jar"]
