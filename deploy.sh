#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

# Step 1: Clean and package the Maven project without running tests
echo "Running gradlew bootjar "
./gradlew clean bootJar

# Step 2: Build the Docker image
echo "Building Docker image..."
docker buildx build --platform linux/amd64,linux/arm64 --load -t api-gateway .

# Step 3: Tag the Docker image
echo "Tagging Docker image..."
docker tag api-gateway:latest kuuku123/api-gateway:latest

# Step 4: Push the Docker image to Docker Hub (if logged in)
if docker info | grep -q "Username"; then
  echo "Pushing Docker image to Docker Hub..."
  docker push kuuku123/api-gateway:latest
else
  echo "Not logged into Docker. Skipping Docker image push."
fi

# Step 5: Bring down any existing Docker Compose services
echo "Stopping and removing existing Docker containers..."
docker-compose -f deploy.yml down

# Step 6: Start Docker Compose services
echo "Starting Docker Compose services..."
docker-compose -f deploy.yml up

echo "Deployment complete!"



