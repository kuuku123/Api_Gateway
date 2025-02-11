# Use a lightweight JDK runtime as the base image
FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY build/libs/api-gateway.jar /app/api-gateway.jar

# Expose the application port
EXPOSE 8083

# Run the application with the prod profile
CMD ["java", "-jar", "api-gateway.jar", "--spring.profiles.active=prod"]

