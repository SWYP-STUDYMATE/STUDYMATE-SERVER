# Use the JAR file built by GitHub Actions
FROM amazoncorretto:21-alpine-jdk
WORKDIR /app

# Copy the pre-built jar file
COPY build/libs/*-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
