# Nexora Application Deployment Guide

This guide covers how to dockerize and deploy the Nexora application for both local testing (using Podman) and production deployment to Digital Ocean droplets.

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Local Development with Podman](#local-development-with-podman)
- [Production Deployment to Digital Ocean](#production-deployment-to-digital-ocean)
- [Configuration Files](#configuration-files)
- [Troubleshooting](#troubleshooting)
- [Monitoring and Maintenance](#monitoring-and-maintenance)

## 🔧 Prerequisites

### For Local Development
- **Podman** (or Docker) installed
- **Podman Compose** (or Docker Compose) installed
- **Node.js 20+** (for Angular frontend build)
- **Java 21** (for Spring Boot backend)
- **Maven 3.9+** (for building the application)

### For Production Deployment
- **Digital Ocean Droplet** with Ubuntu 22.04 LTS (recommended)
- **Docker** and **Docker Compose** installed on the droplet
- **Domain name** (optional but recommended)
- **SSL certificate** (for HTTPS, recommended)

## 🏠 Local Development with Podman

### 1. Build and Run Locally

```bash
# Clone the repository
git clone <your-repo-url>
cd nexora

# Build and run with Podman Compose
podman-compose up --build

# Or with Docker Compose
docker-compose up --build
```

### 2. Access the Application

- **Frontend & Backend**: http://localhost:8080
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **PostgreSQL**: localhost:5432 (nexora/nexora123)

### 3. Development Workflow

```bash
# Stop the application
podman-compose down

# View logs
podman-compose logs -f nexora-app

# Rebuild after changes
podman-compose up --build nexora-app

# Clean up volumes (removes data)
podman-compose down -v
```

## 🚀 Production Deployment to Digital Ocean

### 1. Prepare Your Droplet

```bash
# Connect to your droplet
ssh root@your-droplet-ip

# Update system
apt update && apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Install Docker Compose
apt install docker-compose-plugin -y

# Create non-root user (recommended)
adduser nexora
usermod -aG docker nexora
su - nexora
```

### 2. Deploy the Application

```bash
# Clone your repository
git clone <your-repo-url>
cd nexora

# Copy and configure environment variables
cp .do/.env.prod.template .do/.env.prod
nano .do/.env.prod  # Edit with your production values

# Run the deployment script
./.do/deploy.sh deploy
```

### 3. Environment Configuration

Edit `.do/.env.prod` with your production values:

```bash
# Database Configuration
POSTGRES_PASSWORD=your_secure_database_password

# RabbitMQ Configuration  
RABBITMQ_PASSWORD=your_secure_rabbitmq_password

# JWT Configuration
JWT_SECRET_KEY=your_256_bit_jwt_secret_key

# Digital Ocean Spaces
DO_SPACES_KEY=your_do_spaces_access_key
DO_SPACES_SECRET=your_do_spaces_secret_key
DO_SPACES_BUCKET=your_bucket_name
DO_SPACES_PUBLIC_URL=https://your_bucket_name.fra1.digitaloceanspaces.com
```

### 4. Deployment Commands

```bash
# Deploy application
./.do/deploy.sh deploy

# Check status
./.do/deploy.sh status

# View logs
./.do/deploy.sh logs

# Update application
./.do/deploy.sh update

# Stop application
./.do/deploy.sh stop

# Get help
./.do/deploy.sh help
```

## 📁 Configuration Files

### Docker Files

- **`Dockerfile`**: Multi-stage build for Angular frontend + Spring Boot backend
- **`docker-compose.yml`**: Local development configuration
- **`.do/docker-compose.prod.yml`**: Production configuration with resource limits

### Application Configuration

- **`src/main/resources/application-docker.yml`**: Docker-specific Spring Boot configuration
- **`init-db.sql`**: Database initialization script

### Deployment Files

- **`.do/deploy.sh`**: Automated deployment script
- **`.do/.env.prod.template`**: Environment variables template

## 🔍 Troubleshooting

### Common Issues

#### 1. Application Won't Start

```bash
# Check logs
./.do/deploy.sh logs nexora-app

# Check if database is ready
./.do/deploy.sh logs postgres

# Check if RabbitMQ is ready
./.do/deploy.sh logs rabbitmq
```

#### 2. Database Connection Issues

```bash
# Verify database is running
docker ps | grep postgres

# Check database logs
docker logs nexora-postgres-prod

# Test database connection
docker exec -it nexora-postgres-prod psql -U nexora -d nexora
```

#### 3. Memory Issues

```bash
# Check system resources
free -h
df -h

# Adjust memory limits in .do/docker-compose.prod.yml
# Restart with new limits
./.do/deploy.sh update
```

#### 4. Port Conflicts

```bash
# Check what's using port 8080
sudo netstat -tulpn | grep 8080

# Stop conflicting services or change port in configuration
```

### Health Checks

```bash
# Application health
curl http://localhost:8080/

# Database health
docker exec nexora-postgres-prod pg_isready -U nexora

# RabbitMQ health
docker exec nexora-rabbitmq-prod rabbitmq-diagnostics ping
```

## 📊 Monitoring and Maintenance

### Log Management

```bash
# View real-time logs
./.do/deploy.sh logs

# View specific service logs
docker logs nexora-app-prod

# Rotate logs (add to crontab)
docker system prune -f
```

### Backup Strategy

```bash
# Backup database
docker exec nexora-postgres-prod pg_dump -U nexora nexora > backup_$(date +%Y%m%d).sql

# Backup volumes
docker run --rm -v nexora_postgres_prod_data:/data -v $(pwd):/backup alpine tar czf /backup/postgres_backup_$(date +%Y%m%d).tar.gz /data
```

### Updates

```bash
# Update application code
git pull origin main
./.do/deploy.sh update

# Update Docker images
docker-compose -f .do/docker-compose.prod.yml pull
./.do/deploy.sh deploy
```

### Security Considerations

1. **Firewall Configuration**:
   ```bash
   # Allow only necessary ports
   ufw allow 22    # SSH
   ufw allow 8080  # Application
   ufw enable
   ```

2. **SSL/TLS Setup** (recommended):
   - Use a reverse proxy (Nginx) with Let's Encrypt
   - Configure HTTPS redirects
   - Update application to use secure headers

3. **Regular Updates**:
   - Keep the droplet OS updated
   - Update Docker images regularly
   - Monitor security advisories

### Performance Optimization

1. **Resource Monitoring**:
   ```bash
   # Monitor resource usage
   docker stats
   
   # System monitoring
   htop
   iotop
   ```

2. **Database Optimization**:
   - Configure PostgreSQL for production workloads
   - Set up connection pooling
   - Monitor query performance

3. **Application Tuning**:
   - Adjust JVM heap sizes in production
   - Configure Spring Boot actuator for monitoring
   - Set up application metrics

## 🆘 Support

For issues and questions:

1. Check the application logs first
2. Review this documentation
3. Check the project's issue tracker
4. Contact the development team

---

**Note**: Always test deployments in a staging environment before deploying to production.