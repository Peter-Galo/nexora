# Nexora - Inventory Management System

## Overview
Nexora is a comprehensive inventory management system built with Spring Boot. It provides a robust API for managing products, stock levels, and warehouses, with secure authentication.

## Features
- **Product Management**: Create, update, delete, and search products
- **Warehouse Management**: Manage multiple warehouses and their details
- **Stock Management**: Track stock levels across warehouses
- **Authentication**: Secure API access with JWT-based authentication
- **API Documentation**: Comprehensive API documentation using OpenAPI/Swagger

## Technologies
- Java 17
- Spring Boot 3.5.3
- Spring Data JPA
- Spring Security
- PostgreSQL (Production)
- H2 Database (Testing)
- JWT for Authentication
- OpenAPI/Swagger for API Documentation
- Maven for Build Management

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL (for production)

### Installation
1. Clone the repository
   ```
   git clone https://github.com/yourusername/nexora.git
   cd nexora
   ```

2. Configure the database connection in `src/main/resources/application.yml`

3. Build the project
   ```
   mvn clean install
   ```

4. Run the application
   ```
   mvn spring-boot:run
   ```

### API Documentation
Once the application is running, you can access the API documentation at:
```
http://localhost:8080/swagger-ui.html
```

## API Endpoints

### Authentication
- `POST /api/v1/auth/login` - Authenticate a user
- `POST /api/v1/auth/register` - Register a new user

### Products
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

### Warehouses
- Similar endpoints for warehouse management

### Stock
- Similar endpoints for stock management

## Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

## License
This project is licensed under the MIT License - see the LICENSE file for details.