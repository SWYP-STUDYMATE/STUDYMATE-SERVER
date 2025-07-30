# Stage 1: Build the application with Gradle
FROM gradle:jdk17-jammy AS build
WORKDIR /home/gradle/project

# Copy source code
COPY . .

# Grant execute permission for gradlew
RUN chmod +x ./gradlew

# Build the application, skipping tests
RUN ./gradlew build -x test

# Stage 2: Create the final, lightweight image
FROM amazoncorretto:17-alpine-jdk
WORKDIR /app

# Copy the built jar file from the build stage
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
