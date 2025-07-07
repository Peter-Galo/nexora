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

# Check if Docker or Podman is installed
CONTAINER_ENGINE=""
if command -v podman &> /dev/null; then
    CONTAINER_ENGINE="podman"
    echo -e "${GREEN}✅ Using Podman${NC}"
elif command -v docker &> /dev/null; then
    CONTAINER_ENGINE="docker"
    echo -e "${GREEN}✅ Using Docker${NC}"
else
    echo -e "${RED}❌ Neither Docker nor Podman is installed. Please install one of them first.${NC}"
    exit 1
fi

# Check if Compose is available
if [ "$CONTAINER_ENGINE" = "podman" ]; then
    if ! command -v podman-compose &> /dev/null && ! command -v docker-compose &> /dev/null; then
        echo -e "${RED}❌ podman-compose or docker-compose is not available. Please install one of them.${NC}"
        exit 1
    fi
    # Use docker-compose with podman if available, otherwise podman-compose
    if command -v docker-compose &> /dev/null; then
        COMPOSE_CMD="docker-compose"
    else
        COMPOSE_CMD="podman-compose"
    fi
else
    if ! $CONTAINER_ENGINE compose version &> /dev/null; then
        echo -e "${RED}❌ Docker Compose is not available. Please install Docker Compose.${NC}"
        exit 1
    fi
    COMPOSE_CMD="$CONTAINER_ENGINE compose"
fi

# Check if environment file exists
if [ ! -f "$ENV_FILE" ]; then
    echo -e "${YELLOW}⚠️  Environment file not found: $ENV_FILE${NC}"
    echo "Please create the environment file first using the template."
    echo "Run: cp .do/.env.prod.template .do/.env.prod"
    echo "Then edit .do/.env.prod with your production values."
    exit 1
fi


# Function to build and push Docker images
build_and_push() {
    echo -e "${YELLOW}🔨 Building Docker images...${NC}"

    # Load environment variables
    source "$ENV_FILE"

    # Build the backend image
    echo -e "${YELLOW}🔨 Building backend image...${NC}"
    $CONTAINER_ENGINE build -t "${DOCKER_REGISTRY:-nexora}/${APP_NAME}:${APP_VERSION:-latest}" .

    # Build the frontend image
    echo -e "${YELLOW}🔨 Building frontend image...${NC}"
    $CONTAINER_ENGINE build -t "${DOCKER_REGISTRY:-nexora}/${APP_NAME}-frontend:${APP_VERSION:-latest}" ./client

    # Push to registry if DOCKER_REGISTRY is set
    if [ ! -z "$DOCKER_REGISTRY" ]; then
        echo -e "${YELLOW}📤 Pushing images to registry...${NC}"
        $CONTAINER_ENGINE push "${DOCKER_REGISTRY}/${APP_NAME}:${APP_VERSION:-latest}"
        $CONTAINER_ENGINE push "${DOCKER_REGISTRY}/${APP_NAME}-frontend:${APP_VERSION:-latest}"

    fi
}

# Function to deploy application
deploy() {
    echo -e "${YELLOW}🚀 Deploying application...${NC}"

    # Copy init-db.sql to .do directory for production deployment
    cp init-db.sql .do/

    # Deploy using compose
    if [ "$CONTAINER_ENGINE" = "podman" ] && [ "$COMPOSE_CMD" = "docker-compose" ]; then
        # Use docker-compose with podman
        DOCKER_HOST=unix:///run/user/$UID/podman/podman.sock $COMPOSE_CMD -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" up -d
    else
        $COMPOSE_CMD -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" up -d
    fi

    echo -e "${GREEN}✅ Deployment completed!${NC}"
    echo ""
    echo "Application should be available at: http://your-droplet-ip"
    echo ""
    echo "To check logs:"
    echo "  $COMPOSE_CMD -f $DOCKER_COMPOSE_FILE logs -f nexora-app"
    echo ""
    echo "To check status:"
    echo "  $COMPOSE_CMD -f $DOCKER_COMPOSE_FILE ps"
}

# Function to stop application
stop() {
    echo -e "${YELLOW}🛑 Stopping application...${NC}"

    if [ "$CONTAINER_ENGINE" = "podman" ] && [ "$COMPOSE_CMD" = "docker-compose" ]; then
        DOCKER_HOST=unix:///run/user/$UID/podman/podman.sock $COMPOSE_CMD -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" down
    else
        $COMPOSE_CMD -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" down
    fi

    echo -e "${GREEN}✅ Application stopped!${NC}"
}

# Function to show logs
logs() {

    if [ "$CONTAINER_ENGINE" = "podman" ] && [ "$COMPOSE_CMD" = "docker-compose" ]; then
        DOCKER_HOST=unix:///run/user/$UID/podman/podman.sock $COMPOSE_CMD -f "$DOCKER_COMPOSE_FILE" logs -f "${1:-nexora-app}"
    else
        $COMPOSE_CMD -f "$DOCKER_COMPOSE_FILE" logs -f "${1:-nexora-app}"
    fi
}

# Function to show status
status() {

    if [ "$CONTAINER_ENGINE" = "podman" ] && [ "$COMPOSE_CMD" = "docker-compose" ]; then
        DOCKER_HOST=unix:///run/user/$UID/podman/podman.sock $COMPOSE_CMD -f "$DOCKER_COMPOSE_FILE" ps
    else
        $COMPOSE_CMD -f "$DOCKER_COMPOSE_FILE" ps
    fi
}

# Function to update application
update() {
    echo -e "${YELLOW}🔄 Updating application...${NC}"
    build_and_push

    if [ "$CONTAINER_ENGINE" = "podman" ] && [ "$COMPOSE_CMD" = "docker-compose" ]; then
        DOCKER_HOST=unix:///run/user/$UID/podman/podman.sock $COMPOSE_CMD -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" up -d --force-recreate nexora-app nexora-frontend
    else
        $COMPOSE_CMD -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" up -d --force-recreate nexora-app nexora-frontend
    fi

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
