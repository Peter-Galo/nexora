# Nexora - Enterprise Inventory Management System

<p align="center">
  <img src="https://via.placeholder.com/200x200.png?text=Nexora" alt="Nexora Logo" width="200"/>
</p>

<p align="center">
  <a href="#overview">Overview</a> •
  <a href="#key-features">Key Features</a> •
  <a href="#architecture">Architecture</a> •
  <a href="#technologies">Technologies</a> •
  <a href="#getting-started">Getting Started</a> •
  <a href="#api-documentation">API Documentation</a> •
  <a href="#usage-examples">Usage Examples</a> •
  <a href="#configuration">Configuration</a> •
  <a href="#project-structure">Project Structure</a> •
  <a href="#troubleshooting">Troubleshooting</a> •
  <a href="#contributing">Contributing</a> •
  <a href="#license">License</a>
</p>

## Overview

Nexora is a comprehensive enterprise-grade inventory management system built with Spring Boot. It provides a robust platform for managing products, stock levels, and warehouses across multiple locations. The system offers both REST and GraphQL APIs for flexible integration options, with secure JWT-based authentication and role-based access control.

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
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Client     │────▶│  API Layer  │────▶│  Service    │
│  Applications│     │  (REST/     │     │  Layer      │
└─────────────┘     │   GraphQL)  │     └──────┬──────┘
                    └─────────────┘            │
                           ▲                   ▼
                           │            ┌─────────────┐
                           │            │  Repository │
                           │            │  Layer      │
                           │            └──────┬──────┘
                           │                   │
                           │                   ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Security   │     │  Integration│     │  Database   │
│  Layer      │     │  Services   │     │  (PostgreSQL)│
└─────────────┘     └─────────────┘     └─────────────┘
      │                    │
      ▼                    ▼
┌─────────────┐     ┌─────────────┐
│  JWT Auth   │     │  RabbitMQ   │
│  Service    │     │  DO Spaces  │
└─────────────┘     └─────────────┘
```

## Technologies

### Core Framework
- Java 17
- Spring Boot 3.5.3
- Spring Data JPA
- Spring Security
- Spring WebSocket

### API & Documentation
- Spring Web (REST)
- Spring GraphQL
- OpenAPI/Swagger

### Database
- PostgreSQL (Production)
- H2 Database (Testing)
- Hibernate ORM

### Security
- JWT (JSON Web Tokens)
- BCrypt Password Encoding

### Integration
- RabbitMQ for messaging
- Digital Ocean Spaces (S3-compatible storage)
- Apache POI for Excel export

### Build & Deployment
- Maven
- Docker support

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL (for production)
- RabbitMQ (optional, for messaging features)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/nexora.git
   cd nexora
   ```

2. **Configure the application**

   Edit the configuration files in `src/main/resources/`:
   - `application.yml` - Main configuration
   - `application-dev.yml` - Development environment configuration

   Key configurations:
   - Database connection
   - JWT secret and expiration
   - RabbitMQ settings
   - Digital Ocean Spaces credentials

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   Or with a specific profile:
   ```bash
   mvn spring-boot:run -Dspring.profiles.active=dev
   ```

5. **Verify the application is running**

   Access the Swagger UI at:
   ```
   http://localhost:8080/swagger-ui.html
   ```

   Access the GraphiQL interface at:
   ```
   http://localhost:8080/graphiql
   ```

## API Documentation

### REST API

Once the application is running, you can access the comprehensive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

#### Authentication Endpoints
- `POST /api/v1/auth/login` - Authenticate a user
- `POST /api/v1/auth/register` - Register a new user

#### Product Endpoints
- `GET /api/v1/inventory/products` - Get all products
- `GET /api/v1/inventory/products/active` - Get active products
- `GET /api/v1/inventory/products/{id}` - Get product by ID
- `GET /api/v1/inventory/products/code/{code}` - Get product by code
- `POST /api/v1/inventory/products` - Create a new product
- `PUT /api/v1/inventory/products/{id}` - Update a product
- `DELETE /api/v1/inventory/products/{id}` - Delete a product
- `PUT /api/v1/inventory/products/{id}/activate` - Activate a product
- `PUT /api/v1/inventory/products/{id}/deactivate` - Deactivate a product
- `GET /api/v1/inventory/products/category/{category}` - Get products by category
- `GET /api/v1/inventory/products/brand/{brand}` - Get products by brand
- `GET /api/v1/inventory/products/search?name={name}` - Search products by name

#### Warehouse Endpoints
- `GET /api/v1/inventory/warehouses` - Get all warehouses
- `GET /api/v1/inventory/warehouses/active` - Get active warehouses
- `GET /api/v1/inventory/warehouses/{id}` - Get warehouse by ID
- `GET /api/v1/inventory/warehouses/code/{code}` - Get warehouse by code
- `POST /api/v1/inventory/warehouses` - Create a new warehouse
- `PUT /api/v1/inventory/warehouses/{id}` - Update a warehouse
- `DELETE /api/v1/inventory/warehouses/{id}` - Delete a warehouse
- `PUT /api/v1/inventory/warehouses/{id}/activate` - Activate a warehouse
- `PUT /api/v1/inventory/warehouses/{id}/deactivate` - Deactivate a warehouse

#### Stock Endpoints
- `GET /api/v1/inventory/stocks` - Get all stock records
- `GET /api/v1/inventory/stocks/{id}` - Get stock by ID
- `GET /api/v1/inventory/stocks/product/{productId}` - Get stocks by product ID
- `GET /api/v1/inventory/stocks/product/code/{productCode}` - Get stocks by product code
- `GET /api/v1/inventory/stocks/warehouse/{warehouseId}` - Get stocks by warehouse ID
- `GET /api/v1/inventory/stocks/warehouse/code/{warehouseCode}` - Get stocks by warehouse code
- `GET /api/v1/inventory/stocks/product/{productId}/warehouse/{warehouseId}` - Get stock for a product in a warehouse
- `POST /api/v1/inventory/stocks` - Create a new stock record
- `PUT /api/v1/inventory/stocks/{id}` - Update a stock record
- `DELETE /api/v1/inventory/stocks/{id}` - Delete a stock record
- `PUT /api/v1/inventory/stocks/{id}/add` - Add stock quantity
- `PUT /api/v1/inventory/stocks/{id}/remove` - Remove stock quantity
- `GET /api/v1/inventory/stocks/low` - Get low stock records
- `GET /api/v1/inventory/stocks/over` - Get over stock records
- `GET /api/v1/inventory/stocks/zero` - Get zero stock records

### GraphQL API

Nexora also provides a GraphQL API for more flexible querying. Access the GraphiQL interface at:
```
http://localhost:8080/graphiql
```

#### Example Queries

**Get all products:**
```graphql
query {
  allProducts {
    uuid
    code
    name
    price
    category
    brand
  }
}
```

**Get product by ID:**
```graphql
query {
  productById(id: "product-uuid") {
    uuid
    code
    name
    description
    price
  }
}
```

**Get stocks for a product:**
```graphql
query {
  stocksByProductId(productId: "product-uuid") {
    uuid
    quantity
    warehouse {
      name
      code
    }
  }
}
```

#### Example Mutations

**Create a product:**
```graphql
mutation {
  createProduct(product: {
    code: "PROD-001",
    name: "New Product",
    description: "Product description",
    price: 29.99,
    category: "Electronics",
    brand: "BrandName"
  }) {
    uuid
    code
    name
  }
}
```

**Update stock quantity:**
```graphql
mutation {
  addStock(id: "stock-uuid", quantity: 10) {
    uuid
    quantity
    product {
      name
    }
    warehouse {
      name
    }
  }
}
```

## Usage Examples

### Authentication Flow

1. **Register a new user:**
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/register \
     -H "Content-Type: application/json" \
     -d '{"firstName":"John","lastName":"Doe","email":"john.doe@example.com","password":"securePassword123"}'
   ```

2. **Login to get JWT token:**
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"john.doe@example.com","password":"securePassword123"}'
   ```

3. **Use the JWT token for authenticated requests:**
   ```bash
   curl -X GET http://localhost:8080/api/v1/inventory/products \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"
   ```

### Inventory Management Workflow

1. **Create a warehouse:**
   ```bash
   curl -X POST http://localhost:8080/api/v1/inventory/warehouses \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -d '{"code":"WH-001","name":"Main Warehouse","address":"123 Storage St","city":"Warehouse City","country":"US"}'
   ```

2. **Create a product:**
   ```bash
   curl -X POST http://localhost:8080/api/v1/inventory/products \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -d '{"code":"PROD-001","name":"Smartphone X","description":"Latest smartphone model","price":999.99,"category":"Electronics","brand":"TechBrand"}'
   ```

3. **Add stock for the product in the warehouse:**
   ```bash
   curl -X POST http://localhost:8080/api/v1/inventory/stocks \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -d '{"productId":"PRODUCT_UUID","warehouseId":"WAREHOUSE_UUID","quantity":100,"minStockLevel":20,"maxStockLevel":200}'
   ```

4. **Check stock levels:**
   ```bash
   curl -X GET http://localhost:8080/api/v1/inventory/stocks/product/PRODUCT_UUID \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"
   ```

5. **Remove stock (e.g., for a sale):**
   ```bash
   curl -X PUT http://localhost:8080/api/v1/inventory/stocks/STOCK_UUID/remove?quantity=5 \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"
   ```

## Configuration

### Application Properties

Key configuration properties in `application.yml`:

```yaml
spring:
  application:
    name: nexora
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  graphql:
    graphiql:
      enabled: true
    path: /graphql

# JWT Configuration
application:
  security:
    jwt:
      secret-key: YOUR_SECRET_KEY
      expiration: 86400000 # 24 hours

# OpenAPI Documentation Configuration
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method
    tagsSorter: alpha
    tryItOutEnabled: true
  packages-to-scan: com.nexora.controller
  paths-to-match: /api/**
```

### Environment-Specific Configuration

Development environment configuration in `application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://your-db-host:5432/your-db-name
    username: your-username
    password: your-password
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        default_schema: nexora
        format_sql: false
    show-sql: false

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

# Digital Ocean Spaces Configuration
do:
  spaces:
    key: YOUR_DO_SPACES_KEY
    secret: YOUR_DO_SPACES_SECRET
    region: fra1
    endpoint: https://fra1.digitaloceanspaces.com
    bucket: nexora
    public-url: https://nexora.fra1.digitaloceanspaces.com
```

## Project Structure

```
nexora/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── nexora/
│   │   │           ├── config/           # Configuration classes
│   │   │           ├── controller/       # REST controllers
│   │   │           │   ├── auth/         # Authentication controllers
│   │   │           │   └── inventory/    # Inventory controllers
│   │   │           ├── dto/              # Data Transfer Objects
│   │   │           ├── entity/           # JPA entities
│   │   │           ├── exception/        # Custom exceptions
│   │   │           ├── repository/       # Data repositories
│   │   │           ├── resolver/         # GraphQL resolvers
│   │   │           ├── security/         # Security configuration
│   │   │           ├── service/          # Business logic
│   │   │           └── util/             # Utility classes
│   │   └── resources/
│   │       ├── graphql/                  # GraphQL schema
│   │       └── application.yml           # Application configuration
│   └── test/                             # Test classes
└── pom.xml                               # Maven configuration
```

## Troubleshooting

### Common Issues

**Authentication Issues**
- Ensure you're using the correct JWT token format: `Bearer YOUR_TOKEN`
- Check that your token hasn't expired (default expiration is 24 hours)
- Verify user credentials and permissions

**Database Connection Issues**
- Verify database credentials in application-dev.yml
- Ensure PostgreSQL is running and accessible
- Check database schema exists (default: nexora)

**API Request Problems**
- Validate request format against API documentation
- Check required fields in request bodies
- Verify correct content types (application/json)

### Logging

To enable more detailed logging, add the following to your application.yml:

```yaml
logging:
  level:
    com.nexora: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web: INFO
```

## Contributing

We welcome contributions to Nexora! Here's how you can help:

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Commit your changes**
   ```bash
   git commit -m "Add some feature"
   ```
4. **Push to the branch**
   ```bash
   git push origin feature/your-feature-name
   ```
5. **Open a Pull Request**

### Development Guidelines

- Follow Java code conventions
- Write unit tests for new features
- Update documentation for API changes
- Add appropriate logging
- Ensure all tests pass before submitting PR

## License

This project is licensed under the MIT License - see the LICENSE file for details.

---

<p align="center">
  Made with ❤️ by the Nexora Team
</p>
