# Nexora - Enterprise Inventory Management System

<p align="center">
  <img src="/assets/images/nexora-logo.png?text=Nexora" alt="Nexora Logo" width="100"/>
</p>

<p align="center">
  <a href="#overview">Overview</a> •
  <a href="#key-features">Key Features</a> •
  <a href="#architecture">Architecture</a> •
  <a href="#technologies">Technologies</a> •
  <a href="#quick-start">Quick Start</a> •
  <a href="#local-development">Local Development</a> •
  <a href="#production-deployment">Production Deployment</a> •
  <a href="#api-documentation">API Documentation</a> •
  <a href="#configuration">Configuration</a> •
  <a href="#troubleshooting">Troubleshooting</a> •
  <a href="#contributing">Contributing</a>
</p>

## Overview

Nexora is a comprehensive enterprise-grade inventory management system built with Spring Boot and Angular. It provides a robust platform for managing products, stock levels, and warehouses across multiple locations. The system offers both REST and GraphQL APIs for flexible integration options, with secure JWT-based authentication and role-based access control.

Designed for scalability and performance, Nexora helps businesses efficiently track inventory, manage stock levels, prevent stockouts, and optimize warehouse operations.

## Key Features

- **Product Management**
  - Create, update, delete, and search products
  - Categorize products by brand, category, and custom attributes
  - Track product lifecycle with activation/deactivation

- **Warehouse Management**
  - Manage multiple warehouses and their details
  - Track warehouse locations, capacity, and status
  - Organize warehouses by region, country, or custom criteria

- **Stock Management**
  - Track stock levels across warehouses
  - Set min/max stock levels with alerts for low/over stock
  - Monitor stock movements and history
  - Generate stock reports and analytics

- **Dual API Support**
  - RESTful API with comprehensive endpoints
  - GraphQL API for flexible, client-specific queries
  - Swagger/OpenAPI documentation

- **Security**
  - JWT-based authentication
  - Role-based access control (USER, ADMIN)
  - Stateless architecture for scalability

- **Integration Capabilities**
  - Message queue integration with RabbitMQ
  - File storage with Digital Ocean Spaces (S3-compatible)
  - WebSocket support for real-time updates
  - Excel export functionality

## Architecture

Nexora follows a modern microservices-inspired architecture with clear separation of concerns:

```
┌───────────────┐   ┌─────────────┐      ┌─────────────┐
│  Client       │──▶│  API Layer  │─────▶│  Service    │
│  Applications │   │  (REST/     │      │  Layer      │
└───────────────┘   │   GraphQL)  │      └──────┬──────┘
                    └─────────────┘             │
                           ▲                    ▼
                           │             ┌─────────────┐
                           │             │  Repository │
                           │             │  Layer      │
                           │             └──────┬──────┘
                           │                    │
                           │                    ▼
┌─────────────┐     ┌───────────────┐    ┌────────────────┐
│  Security   │     │  Integration  │    │  Database      │
│  Layer      │     │  Services     │    │  (PostgreSQL)  │
└─────────────┘     └───────────────┘    └────────────────┘
      │                    │
      ▼                    ▼
┌─────────────┐     ┌─────────────┐
│  JWT Auth   │     │  RabbitMQ   │
│  Service    │     │  DO Spaces  │
└─────────────┘     └─────────────┘
```

## Technologies

### Backend
- **Java 17** - Core programming language
- **Spring Boot 3.5.3** - Application framework
- **Spring Data JPA** - Data persistence
- **Spring Security** - Authentication and authorization
- **Spring GraphQL** - GraphQL API support
- **PostgreSQL** - Primary database
- **RabbitMQ** - Message broker
- **JWT** - Token-based authentication

### Frontend
- **Angular 19** - Frontend framework
- **TypeScript** - Programming language
- **Nginx** - Web server and reverse proxy

### DevOps & Deployment
- **Docker/Podman** - Containerization
- **Docker Compose** - Multi-container orchestration
- **Digital Ocean Spaces** - File storage
- **Maven** - Build automation

## Quick Start

### Prerequisites
- **Docker** or **Podman** installed
- **Docker Compose** or **Podman Compose** installed
- **Git** for cloning the repository

### 1. Clone and Start
```bash
# Clone the repository
git clone <your-repo-url>
cd nexora

# Start all services
docker-compose up --build
# OR with Podman
podman-compose up --build
```

### 2. Access the Application
- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080
- **Database**: localhost:5432
- **RabbitMQ Management**: http://localhost:15672

## Local Development

### Environment Setup

1. **Install Dependencies**
   ```bash
   # Docker (Option 1)
   # On macOS with Homebrew
   brew install docker docker-compose

   # On Ubuntu/Debian
   curl -fsSL https://get.docker.com -o get-docker.sh
   sudo sh get-docker.sh
   sudo usermod -aG docker $USER

   # Podman (Option 2)
   # On macOS with Homebrew
   brew install podman podman-compose

   # On Ubuntu/Debian
   sudo apt update
   sudo apt install -y podman podman-compose
   ```

2. **Create Environment File**
   ```bash
   # Copy the template and fill in your values
   cp .env.template .env
   ```

### Development Workflow

```bash
# Start all services
docker-compose up -d

# Start specific service
docker-compose up -d postgres

# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v

# View logs
docker-compose logs -f
docker-compose logs -f nexora-app

# Rebuild after changes
docker-compose up --build nexora-app
```

### Development Tips

1. **Database Access**
   ```bash
   # Connect to PostgreSQL
   docker-compose exec postgres psql -U nexora -d nexora
   ```

2. **Backend Development**
   - Backend runs on port 8080
   - For active development, consider running Spring Boot locally and only database/RabbitMQ in Docker

3. **Frontend Development**
   - Frontend is served by Nginx on port 80
   - For active development, run Angular dev server locally:
   ```bash
   cd client
   npm install
   npm start  # Runs on port 4200
   ```

   **Angular CLI Commands:**
   ```bash
   # Generate new component
   ng generate component component-name

   # Build for production
   ng build

   # Run unit tests
   ng test

   # Run end-to-end tests
   ng e2e

   # Get help with available schematics
   ng generate --help
   ```

### Local Architecture
```
┌─────────────────┐    ┌──────────────────┐
│   Browser       │    │   Nexora App     │
│   localhost     │────│   localhost:8080 │
└─────────────────┘    └──────────────────┘
         │
         ▼
┌─────────────────┐    ┌──────────────────┐
│ Nexora Frontend │    │   PostgreSQL     │
│ Nginx (port 80) │────│   localhost:5432 │
└─────────────────┘    └──────────────────┘
         │
         ▼              ┌──────────────────┐
    API Proxy           │   RabbitMQ       │
    /api/* → :8080      │   localhost:5672 │
                        └──────────────────┘
```

## Production Deployment

### Digital Ocean Deployment

#### Prerequisites
- **Digital Ocean Droplet** with Ubuntu 22.04 LTS
- **Docker** or **Podman** installed on the droplet
- **Domain name** (optional but recommended)

#### 1. Prepare Your Droplet
```bash
# Connect to your droplet
ssh root@your-droplet-ip

# Update system
apt update && apt upgrade -y

# Install Podman (Recommended)
sudo apt update
sudo apt install -y podman podman-compose

# OR Install Docker (Alternative)
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Install docker-compose (if using Podman)
sudo apt install -y docker-compose

# Enable Podman socket (if using Podman)
systemctl --user enable --now podman.socket
```

#### 2. Deploy the Application
```bash
# Clone your repository
git clone <your-repo-url>
cd nexora

# Create production environment file
cp .do/.env.prod.template .do/.env.prod
nano .do/.env.prod  # Edit with your production values

# Make deploy script executable
chmod +x .do/deploy.sh

# Deploy (builds images and starts services)
./.do/deploy.sh deploy
```

#### 3. Environment Configuration

Edit `.do/.env.prod` with your production values:

```bash
# Database Configuration
POSTGRES_DB=nexora
POSTGRES_USER=nexora
POSTGRES_PASSWORD=your_secure_database_password_here

# RabbitMQ Configuration
RABBITMQ_USER=nexora
RABBITMQ_PASSWORD=your_secure_rabbitmq_password_here

# JWT Configuration
JWT_SECRET_KEY=your_jwt_secret_key_here_minimum_256_bits
JWT_EXPIRATION=86400000

# Digital Ocean Spaces Configuration
DO_SPACES_KEY=your_do_spaces_access_key
DO_SPACES_SECRET=your_do_spaces_secret_key
DO_SPACES_REGION=fra1
DO_SPACES_ENDPOINT=https://fra1.digitaloceanspaces.com
DO_SPACES_BUCKET=your_bucket_name
DO_SPACES_PUBLIC_URL=https://your_bucket_name.fra1.digitaloceanspaces.com

# Docker Configuration
DOCKER_REGISTRY=your_docker_registry_url_optional
APP_VERSION=latest
```

#### 4. Deployment Commands

```bash
# Build images only
./.do/deploy.sh build

# Deploy application (default)
./.do/deploy.sh deploy

# Stop application
./.do/deploy.sh stop

# Show logs
./.do/deploy.sh logs [service-name]

# Show status
./.do/deploy.sh status

# Update application
./.do/deploy.sh update

# Show help
./.do/deploy.sh help
```

#### 5. Verify Deployment
```bash
# Check service status
./.do/deploy.sh status

# Check logs
./.do/deploy.sh logs

# Check specific service logs
./.do/deploy.sh logs nexora-app
./.do/deploy.sh logs nexora-frontend
./.do/deploy.sh logs postgres
```

#### 6. Access the Application

After successful deployment:
- **Web Application**: `http://your-droplet-ip`
- **API**: `http://your-droplet-ip/api`
- **RabbitMQ Management** (internal only): `http://127.0.0.1:15672`

#### 7. Firewall Configuration

```bash
# Allow HTTP traffic
sudo ufw allow 80/tcp

# Allow SSH (if not already allowed)
sudo ufw allow 22/tcp

# Enable firewall
sudo ufw enable
```

### Production Architecture
```
┌─────────────────┐
│   Browser       │
│   droplet-ip    │
└─────────────────┘
         │
         ▼
┌─────────────────┐    ┌──────────────────┐
│ Nexora Frontend │    │   Nexora App     │
│ Nginx (port 80) │────│   (internal)     │
└─────────────────┘    └──────────────────┘
         │                       │
    API Proxy                    │
    /api/* → nexora-app:8080     │
                                 ▼
                        ┌──────────────────┐
                        │   PostgreSQL     │
                        │   (internal)     │
                        └──────────────────┘
                                 │
                                 ▼
                        ┌──────────────────┐
                        │   RabbitMQ       │
                        │   (internal)     │
                        └──────────────────┘
```

## API Documentation

### REST API
- **Base URL**: `http://localhost:8080/api`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI Spec**: `http://localhost:8080/v3/api-docs`

### GraphQL API
- **Endpoint**: `http://localhost:8080/graphql`
- **GraphiQL**: `http://localhost:8080/graphiql`

### Authentication
All API endpoints (except public ones) require JWT authentication:

```bash
# Login to get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Use token in subsequent requests
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Configuration

### Environment Variables

#### Required for Production:
- `POSTGRES_PASSWORD` - Database password
- `RABBITMQ_PASSWORD` - RabbitMQ password
- `JWT_SECRET_KEY` - JWT signing key (minimum 256 bits)
- `DO_SPACES_KEY` - Digital Ocean Spaces access key
- `DO_SPACES_SECRET` - Digital Ocean Spaces secret key
- `DO_SPACES_BUCKET` - Digital Ocean Spaces bucket name

#### Optional:
- `POSTGRES_DB` - Database name (default: nexora)
- `POSTGRES_USER` - Database user (default: nexora)
- `RABBITMQ_USER` - RabbitMQ user (default: nexora)
- `JWT_EXPIRATION` - JWT expiration time (default: 86400000ms)
- `DO_SPACES_REGION` - Digital Ocean Spaces region (default: fra1)

### Application Profiles
- `default` - Local development
- `docker` - Docker environment
- `prod` - Production environment

## Troubleshooting

### Local Development Issues

#### Port Conflicts
```bash
# Check what's using the ports
lsof -i :80
lsof -i :8080
lsof -i :5432

# Stop conflicting services or change ports in docker-compose.yml
```

#### Database Issues
```bash
# Reset database
docker-compose down -v
docker-compose up -d postgres
```

#### Permission Issues (Linux/macOS)
```bash
# Fix Docker permissions
sudo usermod -aG docker $USER
# Log out and back in

# For Podman
systemctl --user enable --now podman.socket
```

#### Build Issues
```bash
# Clean build
docker-compose down
docker system prune -f
docker-compose up --build
```

### Production Deployment Issues

#### Check Services Status
```bash
./.do/deploy.sh status
```

#### Check Logs
```bash
./.do/deploy.sh logs
```

#### Restart Services
```bash
./.do/deploy.sh stop
./.do/deploy.sh deploy
```

#### Container Engine Issues
```bash
# For Podman
podman ps -a

# For Docker
docker ps -a
```

#### Common Issues

**Issue**: "Permission denied" when running deploy script
```bash
chmod +x .do/deploy.sh
```

**Issue**: Podman socket not available
```bash
systemctl --user enable --now podman.socket
```

**Issue**: Images not building
- Check disk space: `df -h`
- Check memory: `free -h`

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Spring Boot best practices
- Write unit tests for new features
- Update documentation for API changes
- Use conventional commit messages

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support and questions:
- Create an issue in the GitHub repository
- Check the troubleshooting section above
- Review the API documentation

---

**Note**: This consolidated README combines information from multiple documentation files. For specific deployment scenarios, refer to the individual files in the `.do/` directory for production deployment details.
