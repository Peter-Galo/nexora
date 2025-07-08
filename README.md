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

Nexora is a comprehensive enterprise-grade inventory management system built with Spring Boot and Angular. It provides a
robust platform for managing products, stock levels, and warehouses across multiple locations. The system offers both
REST and GraphQL APIs for flexible integration options, with secure JWT-based authentication and role-based access
control.

Designed for scalability and performance, Nexora helps businesses efficiently track inventory, manage stock levels,
prevent stockouts, and optimize warehouse operations.

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

- **Java 21** - Core programming language
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

- **Podman** - Containerization
- **Podman Compose** - Multi-container orchestration
- **Digital Ocean Spaces** - File storage (optional)
- **Maven** - Build automation

## Quick Start

### Prerequisites

- **Podman** installed
- **Podman Compose** installed
- **Git** for cloning the repository

### 1. Clone and Start

```bash
# Clone the repository
git clone <your-repo-url>
cd nexora

# Start all services
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
podman-compose up -d

# Start specific service
podman-compose up -d db

# Stop all services
podman-compose down

# Stop and remove volumes (clean slate)
podman-compose down -v

# View logs
podman-compose logs -f
podman-compose logs -f backend

# Rebuild after changes
podman-compose up --build backend
```

### Development Tips

1. **Database Access**
   ```bash
   # Connect to PostgreSQL
   podman-compose exec db psql -U nexora -d nexora
   ```

2. **Backend Development**
    - Backend runs on port 8080
    - For active development, consider running Spring Boot locally and only database/RabbitMQ in Podman

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

### Prerequisites

- **Server** with Ubuntu 22.04 LTS (or similar Linux distribution)
- **Podman** and **Podman Compose** installed on the server
- **Domain name** (optional but recommended)

### 1. Prepare Your Server

```bash
# Connect to your server
ssh user@your-server-ip

# Update system
sudo apt update && sudo apt upgrade -y

# Install Podman and Podman Compose
sudo apt update
sudo apt install -y podman podman-compose

# Enable Podman socket for rootless operation
systemctl --user enable --now podman.socket
```

### 2. Deploy the Application

```bash
# Clone your repository
git clone <your-repo-url>
cd nexora

# Create production environment file
cp .env.template .env
nano .env  # Edit with your production values

# Build and start all services
podman-compose build
podman-compose up -d
```

### 3. Environment Configuration

Edit `.env` with your production values (based on `.env.template`):

```bash
DATASOURCE_URL=
DATASOURCE_USERNAME=
DATASOURCE_PASSWORD=

# Database Configuration
POSTGRES_USER=
POSTGRES_PASSWORD=
POSTGRES_DB=

RABBITMQ_HOST=
RABBITMQ_PORT=
RABBITMQ_USERNAME=
RABBITMQ_PASSWORD=
RABBITMQ_QUEUE=
RABBITMQ_EXCHANGE=
RABBITMQ_ROUTING_KEY=

# JWT Configuration (generate a secure key)
JWT_SECRET_KEY=
JWT_EXPIRATION=

# Digital Ocean Spaces
DO_SPACES_KEY=
DO_SPACES_SECRET=
DO_SPACES_REGION=
DO_SPACES_ENDPOINT=
DO_SPACES_BUCKET=
DO_SPACES_PUBLIC_URL=
```

### 4. Deployment Commands

```bash
# Build images
podman-compose build

# Start all services
podman-compose up -d

# Stop all services
podman-compose down

# View logs
podman-compose logs -f

# View specific service logs
podman-compose logs -f backend
podman-compose logs -f frontend
podman-compose logs -f db

# Check service status
podman-compose ps

# Update application (after code changes)
git pull
podman-compose build
podman-compose up -d
```

### 5. Verify Deployment

```bash
# Check service status
podman-compose ps

# Check logs
podman-compose logs

# Test application endpoints
curl http://localhost:8080/api/health  # Backend health check
curl http://localhost  # Frontend
```

### 6. Access the Application

After successful deployment:

- **Web Application**: `http://your-server-ip`
- **API**: `http://your-server-ip:8080/api`
- **RabbitMQ Management**: `http://your-server-ip:15672` (username/password from .env)

### 7. Firewall Configuration

```bash
# Allow HTTP traffic
sudo ufw allow 80/tcp

# Allow API traffic (if needed externally)
sudo ufw allow 8080/tcp

# Allow SSH (if not already allowed)
sudo ufw allow 22/tcp

# Enable firewall
sudo ufw enable
```

### Production Architecture

```
┌─────────────────┐
│   Browser       │
│   server-ip     │
└─────────────────┘
         │
         ▼
┌─────────────────┐    ┌──────────────────┐
│ Nexora Frontend │    │   Nexora Backend │
│ Nginx (port 80) │────│   (port 8080)    │
└─────────────────┘    └──────────────────┘
         │                       │
    API Proxy                    │
    /api/* → backend:8080        │
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

- `dev` - Local development (default profile)
- `prod` - Production and Docker environment
- `test` - Testing environment (uses H2 in-memory database)

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
podman-compose down -v
podman-compose up -d db
```

#### Permission Issues (Linux/macOS)

```bash
# For Podman
systemctl --user enable --now podman.socket
```

#### Build Issues

```bash
# Clean build
podman-compose down
podman system prune -f
podman-compose up --build
```

### Production Deployment Issues

#### Check Services Status

```bash
podman-compose ps
```

#### Check Logs

```bash
podman-compose logs -f
```

#### Restart Services

```bash
podman-compose down
podman-compose up -d
```

#### Container Engine Issues

```bash
# Check running containers
podman ps -a

# Check Podman system info
podman system info
```

#### Common Issues

**Issue**: Podman socket not available

```bash
systemctl --user enable --now podman.socket
```

**Issue**: Images not building

- Check disk space: `df -h`
- Check memory: `free -h`
- Clean up old images: `podman system prune -f`

**Issue**: Port conflicts

```bash
# Check what's using the ports
sudo lsof -i :80
sudo lsof -i :8080
sudo lsof -i :5432
```

**Issue**: Frontend build fails with "io: read/write on closed pipe" during npm install

This error typically occurs during container builds when npm install runs out of resources or encounters network issues.

```bash
# Solutions to try:

# 1. Increase available memory for containers
podman-compose down
podman system prune -f

# 2. Build with more resources (if using systemd)
systemctl --user set-property podman.service MemoryMax=4G

# 3. Try building the frontend separately first
cd client
podman build -t nexora-frontend .

# 4. If the issue persists, try building with Docker instead
docker build -t nexora-frontend .

# 5. Clear npm cache and retry
podman run --rm -v $(pwd)/client:/app -w /app node:18-alpine sh -c "npm cache clean --force && npm ci"
```

The updated Dockerfile includes optimizations to prevent this issue:

- Uses `npm ci` instead of `npm install` for more reliable builds
- Adds retry configuration for network timeouts
- Disables unnecessary output to reduce memory usage

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

## Support

For support and questions:

- Create an issue in the GitHub repository
- Check the troubleshooting section above
- Review the API documentation

---

**Note**: This README provides comprehensive documentation for the Nexora inventory management system. For the latest
updates and additional information, please refer to the project repository.
