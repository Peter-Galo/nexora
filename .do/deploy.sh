#!/bin/bash

# Nexora Application Deployment Script for Digital Ocean
# This script helps deploy the Nexora application to a Digital Ocean droplet

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
APP_NAME="nexora"
DOCKER_COMPOSE_FILE=".do/docker-compose.prod.yml"
ENV_FILE=".do/.env.prod"

echo -e "${GREEN}🚀 Nexora Deployment Script${NC}"
echo "=================================="

# Check if running as root or with sudo
if [[ $EUID -eq 0 ]]; then
   echo -e "${RED}❌ This script should not be run as root${NC}"
   exit 1
fi

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker is not installed. Please install Docker first.${NC}"
    exit 1
fi

# Check if Docker Compose is available
if ! docker compose version &> /dev/null; then
    echo -e "${RED}❌ Docker Compose is not available. Please install Docker Compose.${NC}"
    exit 1
fi

# Check if environment file exists
if [ ! -f "$ENV_FILE" ]; then
    echo -e "${YELLOW}⚠️  Environment file not found: $ENV_FILE${NC}"
    echo "Please create the environment file first using the template."
    echo "Run: cp .do/.env.prod.template .do/.env.prod"
    echo "Then edit .do/.env.prod with your production values."
    exit 1
fi

# Function to build and push Docker image
build_and_push() {
    echo -e "${YELLOW}🔨 Building Docker image...${NC}"
    
    # Load environment variables
    source "$ENV_FILE"
    
    # Build the image
    docker build -t "${DOCKER_REGISTRY:-nexora}/${APP_NAME}:${APP_VERSION:-latest}" .
    
    # Push to registry if DOCKER_REGISTRY is set
    if [ ! -z "$DOCKER_REGISTRY" ]; then
        echo -e "${YELLOW}📤 Pushing image to registry...${NC}"
        docker push "${DOCKER_REGISTRY}/${APP_NAME}:${APP_VERSION:-latest}"
    fi
}

# Function to deploy application
deploy() {
    echo -e "${YELLOW}🚀 Deploying application...${NC}"
    
    # Copy init-db.sql to .do directory for production deployment
    cp init-db.sql .do/
    
    # Deploy using docker-compose
    docker compose -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" up -d
    
    echo -e "${GREEN}✅ Deployment completed!${NC}"
    echo ""
    echo "Application should be available at: http://your-droplet-ip:8080"
    echo ""
    echo "To check logs:"
    echo "  docker compose -f $DOCKER_COMPOSE_FILE logs -f nexora-app"
    echo ""
    echo "To check status:"
    echo "  docker compose -f $DOCKER_COMPOSE_FILE ps"
}

# Function to stop application
stop() {
    echo -e "${YELLOW}🛑 Stopping application...${NC}"
    docker compose -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" down
    echo -e "${GREEN}✅ Application stopped!${NC}"
}

# Function to show logs
logs() {
    docker compose -f "$DOCKER_COMPOSE_FILE" logs -f "${1:-nexora-app}"
}

# Function to show status
status() {
    docker compose -f "$DOCKER_COMPOSE_FILE" ps
}

# Function to update application
update() {
    echo -e "${YELLOW}🔄 Updating application...${NC}"
    build_and_push
    docker compose -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" up -d --force-recreate nexora-app
    echo -e "${GREEN}✅ Application updated!${NC}"
}

# Main script logic
case "${1:-deploy}" in
    "build")
        build_and_push
        ;;
    "deploy")
        build_and_push
        deploy
        ;;
    "stop")
        stop
        ;;
    "logs")
        logs "$2"
        ;;
    "status")
        status
        ;;
    "update")
        update
        ;;
    "help"|"-h"|"--help")
        echo "Usage: $0 [command]"
        echo ""
        echo "Commands:"
        echo "  build   - Build and push Docker image"
        echo "  deploy  - Build, push and deploy application (default)"
        echo "  stop    - Stop the application"
        echo "  logs    - Show application logs (optionally specify service name)"
        echo "  status  - Show application status"
        echo "  update  - Update the application with new image"
        echo "  help    - Show this help message"
        ;;
    *)
        echo -e "${RED}❌ Unknown command: $1${NC}"
        echo "Use '$0 help' to see available commands."
        exit 1
        ;;
esac