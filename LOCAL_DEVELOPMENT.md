# Nexora Local Development Setup

This guide will help you set up and run the Nexora application locally for development.

## Prerequisites

1. **Docker** or **Podman** installed
2. **Docker Compose** or **Podman Compose** installed
3. **Git** for cloning the repository

### Installing Docker (Option 1)
```bash
# On macOS with Homebrew
brew install docker docker-compose

# On Ubuntu/Debian
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
```

### Installing Podman (Option 2)
```bash
# On macOS with Homebrew
brew install podman podman-compose

# On Ubuntu/Debian
sudo apt update
sudo apt install -y podman podman-compose
```

## Local Development Setup

### 1. Clone the Repository
```bash
git clone <your-repo-url>
cd nexora
```

### 2. Create Local Environment File
```bash
cp .env.template .env  # If .env.template exists
# OR create .env manually with required variables
```

### 3. Build and Run Locally
```bash
# Using Docker
docker-compose up --build

# Using Podman
podman-compose up --build
# OR
DOCKER_HOST=unix:///run/user/$UID/podman/podman.sock docker-compose up --build
```

### 4. Access the Application
- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080
- **Database**: localhost:5432
- **RabbitMQ Management**: http://localhost:15672

## Development Workflow

### Starting Services
```bash
# Start all services
docker-compose up -d

# Start specific service
docker-compose up -d postgres
```

### Stopping Services
```bash
# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

### Viewing Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f nexora-app
docker-compose logs -f nexora-frontend
```

### Rebuilding After Changes
```bash
# Rebuild specific service
docker-compose up --build nexora-app

# Rebuild all services
docker-compose up --build
```

## Development Tips

### 1. Database Access
```bash
# Connect to PostgreSQL
docker-compose exec postgres psql -U nexora -d nexora
```

### 2. Backend Development
- Backend runs on port 8080
- Hot reload is not enabled by default in Docker
- For active development, consider running Spring Boot locally and only database/RabbitMQ in Docker

### 3. Frontend Development
- Frontend is served by Nginx on port 80
- For active development, you might want to run Angular dev server locally:
```bash
cd client
npm install
npm start  # Runs on port 4200
```

### 4. Environment Variables
Local development uses these default values:
- Database: `nexora/nexora123`
- RabbitMQ: `nexora/nexora123`
- JWT Secret: Set in `.env` file

## Troubleshooting

### Port Conflicts
If you get port conflicts:
```bash
# Check what's using the ports
lsof -i :80
lsof -i :8080
lsof -i :5432

# Stop conflicting services or change ports in docker-compose.yml
```

### Database Issues
```bash
# Reset database
docker-compose down -v
docker-compose up -d postgres
```

### Permission Issues (Linux/macOS)
```bash
# Fix Docker permissions
sudo usermod -aG docker $USER
# Log out and back in

# For Podman
systemctl --user enable --now podman.socket
```

### Build Issues
```bash
# Clean build
docker-compose down
docker system prune -f
docker-compose up --build
```

## Architecture Overview

### Local Development Stack:
- **Frontend**: Angular app with Nginx (port 80)
- **Backend**: Spring Boot application (port 8080)
- **Database**: PostgreSQL (port 5432)
- **Message Broker**: RabbitMQ (port 5672, management on 15672)

### Network Flow:
1. Browser → Nginx (port 80)
2. Static files served directly by Nginx
3. API calls (`/api/*`) proxied to Spring Boot (port 8080)
4. Spring Boot connects to PostgreSQL and RabbitMQ

This setup mirrors the production environment while exposing additional ports for development convenience.