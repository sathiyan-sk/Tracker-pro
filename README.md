# TrackerPro - JWT Authentication System

A secure Spring Boot application with JWT authentication, role-based authorization, and H2 in-memory database.

## 🚀 Features

- **JWT Authentication**: Secure token-based authentication system
- **Role-Based Authorization**: Four user roles - ADMIN, HR, FACULTY, STUDENT
- **H2 Database**: In-memory database for testing
- **Password Encryption**: BCrypt password encoding
- **Validation**: Comprehensive client-side and server-side validation
- **Exception Handling**: Global exception handler for consistent error responses
- **CORS Support**: Configured for cross-origin requests

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+ or use the included Maven wrapper (mvnw)
- A modern web browser

## 🛠️ Tech Stack

- **Backend**: Spring Boot 4.0.0, Spring Security, JWT (JJWT 0.12.5)
- **Database**: H2 (in-memory), MySQL ready for production
- **Frontend**: HTML5, CSS3, JavaScript (Vanilla)
- **Build Tool**: Maven

## 📦 Installation

### 1. Clone the Repository

```bash
git clone <repository-url>
cd Tracker-pro
```

### 2. Build the Project

```bash
# Using Maven wrapper (recommended)
./mvnw clean install -DskipTests

# Or using Maven
mvn clean install -DskipTests
```

### 3. Run the Application

```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or run the JAR directly
java -jar target/Tracker-pro-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

## 🔐 Default Admin Credentials

A default admin user is automatically created on application startup:

- **Email**: `admin@trackerpro.com`
- **Password**: `admin123`
- **Role**: ADMIN

## 📚 API Endpoints

### Authentication Endpoints

#### 1. Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "password123",
  "mobileNo": "9876543210",
  "gender": "Male",
  "dob": "01/01/2000",
  "age": 24,
  "location": "Mumbai",
  "userType": "STUDENT"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Registration successful",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "userType": "STUDENT",
    "mobileNo": "9876543210"
  }
}
```

#### 2. Login User
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "userType": "STUDENT",
    "mobileNo": "9876543210"
  }
}
```

#### 3. Health Check
```http
GET /api/auth/health
```

**Response:**
```
Auth service is running
```

### Protected Endpoints

For accessing protected endpoints, include the JWT token in the Authorization header:

```http
Authorization: Bearer <your-jwt-token>
```

## 🗃️ Database Configuration

### H2 Database (Current - Testing)

The application uses H2 in-memory database by default:

- **Console URL**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:trackerpro_db`
- **Username**: `sa`
- **Password**: _(empty)_

### MySQL Database (Production Ready)

To switch to MySQL, update `application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/trackerpro_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
```

## 🎭 User Roles

1. **STUDENT**: Default role for registered users
2. **HR**: Human Resources role
3. **FACULTY**: Teaching staff role
4. **ADMIN**: Administrator role with full access

## 🔒 Security Features

### JWT Configuration

- **Secret Key**: Configured in `application.yaml`
- **Token Expiration**: 24 hours (86400000 ms)
- **Algorithm**: HS256

### Password Encoding

- Uses BCrypt with strength 10 for password hashing
- Passwords are never stored in plain text

### Validation Rules

#### Registration:
- **First Name**: 2-30 characters, letters only
- **Email**: Valid email format, 6-100 characters
- **Password**: Minimum 6 characters
- **Mobile**: 10-digit Indian mobile number (starts with 6-9)
- **Age**: 20-25 years (configurable)
- **Gender**: Male, Female, or Other

#### Login:
- **Email**: Required, valid email format
- **Password**: Required, minimum 6 characters

## 📱 Frontend Pages

### 1. Login Page (`loginPage.html`)
- Email and password authentication
- Password visibility toggle
- Client-side validation
- Error handling and success messages
- Token storage in localStorage

### 2. Register Page (`registerPage.html`)
- Complete user registration form
- Date picker for DOB
- Real-time validation
- Mobile number formatting
- Rate limiting protection

### 3. Home Page (`index.html`)
- Landing page after successful login

## 🧪 Testing

### Using cURL

#### Test Admin Login:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@trackerpro.com","password":"admin123"}'
```

#### Test Student Registration:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "email": "test@student.com",
    "password": "test123",
    "mobileNo": "9123456789",
    "gender": "Male",
    "dob": "01/01/2002",
    "age": 22,
    "location": "Delhi",
    "userType": "STUDENT"
  }'
```

### Using Browser

1. Navigate to `http://localhost:8080`
2. Click on "Register here" to create a new account
3. Fill in the registration form and submit
4. After successful registration, you'll be redirected to login
5. Login with your credentials or use the admin account

## 📂 Project Structure

```
Tracker-pro/
├── src/
│   ├── main/
│   │   ├── java/com/webapp/Tracker_pro/
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── DataInitializer.java
│   │   │   ├── controller/
│   │   │   │   └── AuthController.java
│   │   │   ├── dto/
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── AuthResponse.java
│   │   │   │   └── ErrorResponse.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── UserAlreadyExistsException.java
│   │   │   │   ├── InvalidCredentialsException.java
│   │   │   │   └── ResourceNotFoundException.java
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   └── UserType.java
│   │   │   ├── repository/
│   │   │   │   └── UserRepository.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── JwtService.java
│   │   │   │   └── UserService.java
│   │   │   └── TrackerProApplication.java
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── index.html
│   │       │   ├── loginPage.html
│   │       │   ├── registerPage.html
│   │       │   ├── LOGO.png
│   │       │   └── illustration.png
│   │       └── application.yaml
│   └── test/
├── pom.xml
└── README.md
```

## ⚙️ Configuration

### JWT Settings (`application.yaml`)

```yaml
jwt:
  secret: 3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b
  expiration: 86400000  # 24 hours
```

### Server Settings

```yaml
server:
  port: 8080
  error:
    include-message: always
    include-binding-errors: always
```

## 🐛 Error Handling

The application provides detailed error responses:

### Validation Error Example:
```json
{
  "success": false,
  "message": "Validation failed",
  "status": 400,
  "timestamp": "2025-11-21T09:30:00",
  "errors": {
    "email": "Please enter a valid email address",
    "password": "Password must be at least 6 characters"
  }
}
```

### Authentication Error Example:
```json
{
  "success": false,
  "message": "Invalid email or password",
  "status": 401
}
```

## 🔄 Token Refresh

JWT tokens are valid for 24 hours. After expiration, users need to log in again.
Token refresh functionality can be implemented in future versions.

## 🚀 Deployment

### Production Checklist

- [ ] Change JWT secret key in `application.yaml`
- [ ] Switch to MySQL database
- [ ] Update CORS configuration for production domains
- [ ] Set `hibernate.ddl-auto` to `validate` or `none`
- [ ] Remove H2 console in production
- [ ] Configure proper logging
- [ ] Set up SSL/TLS certificates
- [ ] Implement token refresh mechanism
- [ ] Add rate limiting
- [ ] Set up monitoring and alerts

## 📄 License

This project is part of the TrackerPro system.

## 👥 Support

For issues and questions, please contact the development team.

---

**Version**: 0.0.1-SNAPSHOT  
**Last Updated**: November 2025
