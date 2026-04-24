#!/bin/bash
set -e

echo "Running gradlew bootJar"
./gradlew clean bootJar

echo "Building Docker image..."
docker buildx build --load -t api-gateway .

echo "Tagging Docker image..."
docker tag api-gateway:latest kuuku123/api-gateway:latest

if docker info 2>/dev/null | grep -q "Username"; then
  echo "Pushing Docker image to Docker Hub..."
  docker push kuuku123/api-gateway:latest
else
  echo "Not logged into Docker. Skipping push."
fi

echo "Stopping existing containers..."
docker compose -f deploy.yml down

echo "Starting services..."
docker compose -f deploy.yml up -d

echo "Deployment complete!"
