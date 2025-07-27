# User Service Documentation

## Overview

The User Service handles authentication, authorization, and user management. It implements JWT-based authentication with refresh token rotation and provides comprehensive security features.

## Service Details

- **Port**: 8084
- **Technology**: Spring Boot 3.2, Spring Security 6
- **Database**: PostgreSQL with JPA

## Features

### Authentication
- JWT token generation and validation
- Refresh token rotation for enhanced security
- Multi-factor authentication support
- Session management

### Authorization
- Role-based access control (RBAC)
- Method-level security annotations
- Resource-based permissions

### User Management
- User registration and profile management
- Password hashing with BCrypt
- Account activation and deactivation

## API Endpoints

### Authentication Endpoints

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "trader@example.com",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "username": "trader@example.com",
  "email": "trader@example.com",
  "roles": ["ROLE_USER"]
}
```

#### Refresh Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer"
}
```

#### Logout
```http
POST /api/auth/logout
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

### User Management Endpoints

#### Get User Profile
```http
GET /api/users/profile
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

#### Update User Profile
```http
PUT /api/users/profile
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "fcmToken": "firebase-token"
}
```

## Security Configuration

### JWT Configuration
- **Algorithm**: HS512
- **Token Expiration**: 24 hours
- **Refresh Token Expiration**: 7 days
- **Secret Rotation**: Daily

### Password Security
- **Hashing**: BCrypt with strength 12
- **Minimum Length**: 8 characters
- **Complexity Requirements**: Mixed case, numbers, symbols

### Rate Limiting
- **Login Attempts**: 5 per 15 minutes
- **Registration**: 3 per hour
- **Password Reset**: 3 per hour

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(120) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone_number VARCHAR(20),
    fcm_token VARCHAR(255),
    created_date TIMESTAMP DEFAULT NOW(),
    last_modified_date TIMESTAMP DEFAULT NOW()
);
```

### Roles Table
```sql
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL
);

CREATE TABLE user_roles (
    user_id UUID REFERENCES users(id),
    role_id INT REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);
```

### Refresh Tokens Table
```sql
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    token VARCHAR(255) UNIQUE NOT NULL,
    expiry_date TIMESTAMP NOT NULL
);
```

## Error Handling

### Authentication Errors
```json
{
  "timestamp": "2024-01-01T12:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid credentials",
  "path": "/api/auth/login"
}
```

### Token Errors
```json
{
  "timestamp": "2024-01-01T12:00:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access token expired",
  "path": "/api/users/profile"
}
```

## Configuration

### Environment Variables
```yaml
TASTYTRADE_APP_JWT_SECRET: ${JWT_SECRET}
TASTYTRADE_APP_JWT_EXPIRATION_MS: 86400000
TASTYTRADE_APP_JWT_REFRESH_EXPIRATION_MS: 604800000
DATABASE_URL: jdbc:postgresql://timescaledb:5432/tastytrade
DATABASE_USERNAME: tastytrade
DATABASE_PASSWORD: ${DB_PASSWORD}
```

### Health Checks
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

## Deployment

### Resource Requirements
```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "500m"
```

### Scaling Configuration
- **Min Replicas**: 2
- **Max Replicas**: 10
- **Target CPU**: 70%
- **Target Memory**: 80%