# Nexora Digital Ocean Deployment Guide

This guide will help you deploy the Nexora application to a Digital Ocean droplet using either Docker or Podman.

## Prerequisites

### On your Digital Ocean Droplet:

1. **Install Podman (Recommended)** or Docker:
   ```bash
   # For Ubuntu/Debian - Install Podman
   sudo apt update
   sudo apt install -y podman podman-compose
   
   # OR Install Docker (alternative)
   curl -fsSL https://get.docker.com -o get-docker.sh
   sudo sh get-docker.sh
   sudo usermod -aG docker $USER
   ```

2. **Install docker-compose** (if using Podman):
   ```bash
   sudo apt install -y docker-compose
   ```

3. **Enable Podman socket** (if using Podman):
   ```bash
   systemctl --user enable --now podman.socket
   ```

## Deployment Steps

### 1. Clone the Repository
```bash
git clone <your-repo-url>
cd nexora
```

### 2. Create Production Environment File
```bash
cp .do/.env.prod.template .do/.env.prod
```

Edit `.do/.env.prod` with your production values:
```bash
nano .do/.env.prod
```

**Required values to set:**
- `POSTGRES_PASSWORD`: Strong database password
- `RABBITMQ_PASSWORD`: Strong RabbitMQ password  
- `JWT_SECRET_KEY`: Strong JWT secret (minimum 256 bits)
- `DO_SPACES_KEY`: Your Digital Ocean Spaces access key
- `DO_SPACES_SECRET`: Your Digital Ocean Spaces secret key
- `DO_SPACES_BUCKET`: Your Digital Ocean Spaces bucket name
- `DO_SPACES_PUBLIC_URL`: Your bucket's public URL

### 3. Deploy the Application
```bash
# Make the deploy script executable
chmod +x .do/deploy.sh

# Deploy (builds images and starts services)
./.do/deploy.sh deploy
```

### 4. Verify Deployment
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

## Available Commands

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

## Accessing the Application

After successful deployment:
- **Web Application**: `http://your-droplet-ip`
- **API**: `http://your-droplet-ip/api`
- **RabbitMQ Management** (internal only): `http://127.0.0.1:15672`

## Firewall Configuration

Make sure your droplet's firewall allows:
```bash
# Allow HTTP traffic
sudo ufw allow 80/tcp

# Allow SSH (if not already allowed)
sudo ufw allow 22/tcp

# Enable firewall
sudo ufw enable
```

## Troubleshooting

### 1. Check if services are running:
```bash
./.do/deploy.sh status
```

### 2. Check logs for errors:
```bash
./.do/deploy.sh logs
```

### 3. Restart services:
```bash
./.do/deploy.sh stop
./.do/deploy.sh deploy
```

### 4. Check container engine:
```bash
# For Podman
podman ps -a

# For Docker
docker ps -a
```

### 5. Common Issues:

**Issue**: "Permission denied" when running deploy script
```bash
chmod +x .do/deploy.sh
```

**Issue**: Podman socket not available
```bash
systemctl --user enable --now podman.socket
```

**Issue**: Images not building
- Check if you have enough disk space: `df -h`
- Check if you have enough memory: `free -h`

## Production Considerations

1. **SSL/TLS**: Consider adding SSL certificates using Let's Encrypt
2. **Backups**: Set up regular database backups
3. **Monitoring**: Consider adding monitoring tools
4. **Updates**: Use the `update` command for application updates
5. **Security**: Regularly update the system and change default passwords

## Architecture

The deployment includes:
- **PostgreSQL**: Database server (internal access only)
- **RabbitMQ**: Message broker (management UI on localhost:15672)
- **Nexora Backend**: Spring Boot application (internal access only)
- **Nexora Frontend**: Angular app with Nginx reverse proxy (public access on port 80)

The frontend Nginx serves static files and proxies API requests to the backend, providing a single entry point for the application.