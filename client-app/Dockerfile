# ---- Build Stage ----
FROM maven:3.8.7-openjdk-18-slim AS build

WORKDIR /build

# Copy the entire project to preserve parent POM structure
COPY . .

# Build only the client module
RUN mvn -pl client-app -am clean package -DskipTests

# ---- Runtime Stage ----
FROM eclipse-temurin:18-jdk-alpine

WORKDIR /app

# Copy only the final jar from the correct module
COPY --from=build /build/client-app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]