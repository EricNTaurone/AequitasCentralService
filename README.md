# Aequitas Central Service

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/EricNTaurone/AequitasCentralService)
[![Version](https://img.shields.io/badge/version-0.1.0--SNAPSHOT-blue)](https://github.com/EricNTaurone/AequitasCentralService)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java](https://img.shields.io/badge/java-21-orange)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen)](https://spring.io/projects/spring-boot)

> Enterprise-grade multi-tenant legal timekeeping service with strict isolation, RBAC workflows, and event-driven architecture

Aequitas Central Service is a production-ready Spring Boot application that provides secure, multi-tenant time tracking capabilities for legal firms. Built with hexagonal architecture principles, it enforces strict tenant isolation through PostgreSQL row-level security, implements exactly-once semantics via idempotent APIs, and publishes domain events through a transactional outbox pattern.

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [API Documentation](#api-documentation)
- [Development](#development)
- [Testing](#testing)
- [Deployment](#deployment)
- [Observability](#observability)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

---

## Features

### Core Capabilities

- **Multi-Tenant Architecture**: Complete firm isolation with PostgreSQL row-level security (RLS)
- **User Registration & Authentication**: Self-service user signup with firm association and JWT token-based authentication
- **Role-Based Access Control (RBAC)**: Three-tier permission model (Employee, Manager, Admin)
- **Time Entry Management**: Full lifecycle support from creation through approval
- **Firm Management**: Complete CRUD operations for managing law firm profiles and settings
- **Idempotent Operations**: Guarantee exactly-once semantics with retry protection
- **Event-Driven Integration**: Transactional outbox pattern for reliable event publishing
- **OAuth2/JWT Authentication**: Secure token-based authentication with claim propagation
- **Comprehensive Audit Trail**: Track all changes with correlation IDs and structured logging
- **Production-Ready Observability**: Metrics, health checks, and distributed tracing

### Key Technical Features

- Hexagonal (Ports & Adapters) architecture for maintainable, testable code
- Database migrations with Flyway for versioned schema evolution
- Docker-based deployment with distroless containers
- 100% code coverage for domain and application layers
- Mutation testing with PITest (90% threshold)
- OpenAPI/Swagger documentation
- Prometheus metrics integration

---

## Architecture

Aequitas Central Service follows **hexagonal architecture** (ports and adapters) to maintain clean separation of concerns and enable independent evolution of components.

### Architectural Principles

1. **Hexagonal Layering**: Controllers communicate with inbound ports, application services orchestrate domain logic, and adapters implement outbound ports
2. **Multi-Tenancy & RBAC**: JWT claims (`firm_id`, `role`) propagate through `TenantContextFilter` and a tenant-aware `DataSource` that sets PostgreSQL GUCs to activate row-level security policies
3. **Idempotent Commands**: `IdempotencyService` stores keyed responses to guarantee exactly-once semantics under retries
4. **Transactional Outbox**: Approvals append `ENTRY_APPROVED.v1` events to the outbox table which `OutboxRelay` publishes through the pluggable `EventPublisher`
5. **Observability**: Springdoc OpenAPI, Micrometer/Actuator, correlation-id filter, and JSON logging

### Directory Structure

```
src/main/java/com/aequitas/aequitascentralservice/
├── AequitasCentralServiceApplication.java  # Spring Boot main class
├── adapter/
│   ├── outbox/               # Outbox pattern implementation for events
│   ├── persistence/          # JPA entities, repositories, database adapters
│   ├── security/             # JWT to CurrentUser mapping, auth adapters
│   ├── supabase/             # Supabase authentication integration
│   └── web/                  # REST controllers, DTOs, exception handlers
├── app/
│   ├── port/                 # Inbound/outbound contracts (interfaces)
│   └── service/              # Domain use cases, idempotency logic
├── config/                   # Application configuration beans
├── constants/                # Application-wide constants and enums
├── domain/                   # Aggregates, commands, domain events
├── logging/                  # Correlation ID filter, structured logging
├── security/                 # OAuth2 resource server configuration
└── tenancy/                  # Thread-local context, tenant-aware datasource

src/main/resources/
├── application.properties    # Base Spring Boot properties
├── application.yml           # Main application configuration
├── application-dev.yml       # Development profile overrides
├── db/migration/             # Flyway database migrations
├── graphql/                  # GraphQL schemas (if applicable)
├── openapi/                  # OpenAPI/Swagger specifications
│   └── aequitas-api.yaml     # REST API specification
├── logback-spring.xml        # Logging configuration
├── static/                   # Static web resources
└── templates/                # Template files (e.g., email templates)
```

### Data Flow

```
[Client Request] → [TenantContextFilter] → [Controller] → [Inbound Port]
                                                                ↓
                                                       [Application Service]
                                                                ↓
                                                    [Domain Aggregate/Command]
                                                                ↓
                                           [Outbound Ports: Persistence, Events]
                                                                ↓
                                              [JPA Adapter / Outbox Writer]
                                                                ↓
                                              [PostgreSQL with RLS Policies]
```

### Authentication Flow

Aequitas Central Service integrates with Supabase for user authentication and session management:

1. **User Registration** (`/api/v1/auth/signup`):
   - Client submits email, password, firm ID, and desired role
   - Service validates firm existence and creates Supabase user account
   - User profile is stored with firm association and role assignment
   - Supabase returns session tokens (access + refresh)

2. **User Authentication** (`/api/v1/auth/signin`):
   - Client submits email and password credentials
   - Service validates credentials against Supabase
   - On success, returns fresh JWT tokens with embedded claims
   - Tokens include: `sub` (user ID), `firm_id` (tenant), `role` (RBAC)

3. **Authenticated Requests**:
   - Client includes JWT in `Authorization: Bearer <token>` header
   - `TenantContextFilter` extracts and validates token claims
   - Tenant context propagates through request lifecycle
   - PostgreSQL RLS policies enforce data isolation based on `firm_id`

4. **Token Refresh**:
   - Clients use refresh token to obtain new access tokens
   - Access tokens expire after configured duration (default: 1 hour)
   - Refresh tokens have longer validity (default: 7 days)

---

## Prerequisites

### Required

- **Java 21** or higher ([Eclipse Temurin](https://adoptium.net/) recommended)
- **Docker** 20.10+ and Docker Compose 1.29+ (for local development)
- **Maven** 3.8+ (or use included Maven wrapper `./mvnw`)
- **PostgreSQL** 16+ (managed via Docker Compose)

### Recommended

- **Git** 2.30+ for version control
- **curl** or **Postman** for API testing
- **IntelliJ IDEA** or **VS Code** with Java extensions

### System Requirements

- 4 GB RAM minimum (8 GB recommended)
- 2 CPU cores minimum
- 2 GB disk space for dependencies and containers

---

## Installation

### Quick Start

1. **Clone the repository**

```bash
git clone https://github.com/EricNTaurone/AequitasCentralService.git
cd AequitasCentralService
```

2. **Build the project**

```bash
./mvnw clean install -DskipTests
```

3. **Start infrastructure with Docker Compose**

```bash
docker compose up -d
```

4. **Run the application**

```bash
./mvnw spring-boot:run
```

The service will be available at `http://localhost:8080`

### Offline/Air-Gapped Installation

For environments without internet access:

```bash
# Pre-download dependencies to local repository
./mvnw -Dmaven.repo.local=.m2 dependency:go-offline

# Build using local repository
./mvnw -Dmaven.repo.local=.m2 clean verify
```

### Docker-Only Installation

```bash
cd docker
docker compose up --build
```

This starts:
- **PostgreSQL** on port `6543` (mapped from container 5432)
- **Aequitas Central Service** on port `8080`

---

## Configuration

### Environment Variables

The application supports configuration via environment variables or Spring profiles.

#### Core Configuration

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/aequitas_central` | Yes |
| `SPRING_DATASOURCE_USERNAME` | Database username | `aequitas` | Yes |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `aequitas` | Yes |
| `SECURITY_JWT_SECRET` | HMAC secret for JWT validation | `change-me-in-prod` | Yes |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` | No |

#### Authentication Configuration

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SUPABASE_URL` | Supabase project URL | - | Yes |
| `SUPABASE_KEY` | Supabase anonymous/public key | - | Yes |
| `SUPABASE_JWT_SECRET` | Supabase JWT secret for token verification | Same as `SECURITY_JWT_SECRET` | Yes |

#### JWT Configuration

Tokens must include the following claims:

- `sub`: User email/identifier
- `firm_id`: UUID of the user's law firm
- `role`: One of `EMPLOYEE`, `MANAGER`, `ADMIN`

### Configuration Files

#### `application.yml`

```yaml
spring:
  application:
    name: aequitas-central-service
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: none
  flyway:
    enabled: true
    baseline-on-migrate: true
```

#### `application-dev.yml`

Developer-specific overrides for local development. Contains relaxed security settings and verbose logging.

### Database Setup

Flyway migrations automatically run on application startup. The baseline migration (`V1__baseline.sql`) creates:

- Multi-tenant schema with `firm_id` columns
- Row-level security policies enforcing tenant isolation
- Core tables: `firms`, `user_profiles`, `customers`, `projects`, `time_entries`
- Support tables: `idempotency_records`, `outbox`, `integration_configs`

---

## Usage

### Basic Workflow

1. **Register a new user account** (if needed)
2. **Authenticate and obtain JWT token**
3. **Create a time entry** (Employee role)
4. **Submit for approval** (Employee role)
5. **Approve time entry** (Manager or Admin role)
6. **Query approved entries**

### Example: User Registration

```bash
curl -X POST http://localhost:8080/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "firmId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "email": "john.doe@lawfirm.com",
    "password": "SecurePass123!",
    "role": "EMPLOYEE"
  }'
```

Response:
```json
{
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "john.doe@lawfirm.com",
    "firmId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "role": "EMPLOYEE"
  },
  "tokens": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600,
    "tokenType": "bearer"
  }
}
```

### Example: User Authentication

```bash
curl -X POST http://localhost:8080/api/v1/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@lawfirm.com",
    "password": "SecurePass123!"
  }'
```

Response:
```json
{
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "john.doe@lawfirm.com",
    "firmId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "role": "EMPLOYEE"
  },
  "tokens": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600,
    "tokenType": "bearer"
  }
}
```

### Example: Create Time Entry

```bash
curl -X POST http://localhost:8080/api/v1/entries \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: create-entry-123" \
  -d '{
    "customerId": "550e8400-e29b-41d4-a716-446655440000",
    "projectId": "660e8400-e29b-41d4-a716-446655440001",
    "narrative": "Client consultation and contract review",
    "durationMinutes": 90
  }'
```

### Example: Submit Time Entry

```bash
curl -X POST http://localhost:8080/api/v1/entries/{entryId}/submit \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Idempotency-Key: submit-entry-123"
```

### Example: Approve Time Entry (Manager)

```bash
curl -X POST http://localhost:8080/api/v1/entries/{entryId}/approve \
  -H "Authorization: Bearer MANAGER_JWT_TOKEN" \
  -H "Idempotency-Key: approve-entry-123"
```

### Example: Query Time Entries

```bash
# Get all accessible entries (paginated)
curl http://localhost:8080/api/v1/entries?page=0&size=20 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Get specific entry
curl http://localhost:8080/api/v1/entries/{entryId} \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Example: User Management

```bash
# Get current user profile
curl http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# List users in firm (Manager/Admin only)
curl http://localhost:8080/api/v1/users?role=EMPLOYEE \
  -H "Authorization: Bearer MANAGER_JWT_TOKEN"

# Update user role (Admin only)
curl -X PATCH http://localhost:8080/api/v1/users/{userId}/role \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"role": "MANAGER"}'
```

### Example: Firm Management

```bash
# Get current user's firm
curl http://localhost:8080/api/v1/firms/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Get specific firm by ID (Admin only)
curl http://localhost:8080/api/v1/firms/f47ac10b-58cc-4372-a567-0e02b2c3d479 \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"

# List all firms with pagination (Admin only)
curl "http://localhost:8080/api/v1/firms?limit=20&cursor=eyJpZCI6IjEyMyJ9" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"

# Create a new firm (System Admin only)
curl -X POST http://localhost:8080/api/v1/firms \
  -H "Authorization: Bearer SYSTEM_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: create-firm-$(uuidgen)" \
  -d '{
    "name": "Smith & Associates Law Firm",
    "address": {
      "street": "123 Main Street",
      "city": "New York",
      "state": "NY",
      "postalCode": "10001",
      "country": "USA"
    }
  }'

# Update firm details (Admin only)
curl -X PATCH http://localhost:8080/api/v1/firms/f47ac10b-58cc-4372-a567-0e02b2c3d479 \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Smith & Partners LLP",
    "address": {
      "street": "456 Park Avenue",
      "city": "New York",
      "state": "NY",
      "postalCode": "10022",
      "country": "USA"
    }
  }'
```

---

## API Documentation

### Interactive Documentation

Once the application is running, access the Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI specification available at:

```
http://localhost:8080/v3/api-docs
```

### Core Endpoints

#### Authentication

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| `POST` | `/api/v1/auth/signup` | Register a new user with firm association | None (Public) |
| `POST` | `/api/v1/auth/signin` | Authenticate user and obtain JWT tokens | None (Public) |

#### Time Entries

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| `POST` | `/api/v1/entries` | Create new time entry | EMPLOYEE |
| `GET` | `/api/v1/entries` | List time entries (paginated) | EMPLOYEE |
| `GET` | `/api/v1/entries/{id}` | Get specific time entry | EMPLOYEE |
| `PATCH` | `/api/v1/entries/{id}` | Update time entry | EMPLOYEE (own entries) |
| `POST` | `/api/v1/entries/{id}/submit` | Submit for approval | EMPLOYEE (own entries) |
| `POST` | `/api/v1/entries/{id}/approve` | Approve time entry | MANAGER, ADMIN |

#### User Management

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| `GET` | `/api/v1/users/me` | Get current user profile | EMPLOYEE |
| `GET` | `/api/v1/users` | List firm users | MANAGER, ADMIN |
| `PATCH` | `/api/v1/users/{id}/role` | Update user role | ADMIN |

#### Firm Management

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| `GET` | `/api/v1/firms/me` | Get current user's firm information | EMPLOYEE |
| `GET` | `/api/v1/firms/{id}` | Get specific firm by ID | ADMIN |
| `GET` | `/api/v1/firms` | List all firms (paginated) | ADMIN |
| `POST` | `/api/v1/firms` | Create a new firm | SYSTEM_ADMIN |
| `PATCH` | `/api/v1/firms/{id}` | Update firm details | ADMIN |

### Idempotency

All state-changing operations (POST, PATCH) support idempotency via the `Idempotency-Key` header:

```bash
Idempotency-Key: unique-operation-identifier-123
```

Replaying a request with the same key within 24 hours returns the cached response without side effects.

---

## Development

### Local Development Setup

1. **Start PostgreSQL**

```bash
docker compose up -d postgres
```

2. **Run application in development mode**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

3. **Enable hot reload** (optional)

Add Spring Boot DevTools dependency for automatic restarts on code changes.

### Code Quality Tools

The project enforces strict quality standards:

| Tool | Purpose | Configuration |
|------|---------|---------------|
| **Spotless** | Code formatting (Google Java Style) | `pom.xml` |
| **Checkstyle** | Static analysis | `config/checkstyle/checkstyle.xml` |
| **PMD** | Bug detection | `config/pmd/pmd-ruleset.xml` |
| **SpotBugs** | Bug patterns | `config/spotbugs/exclude.xml` |
| **JaCoCo** | Code coverage (100% domain/service) | `pom.xml` |
| **PITest** | Mutation testing (90% threshold) | `pom.xml` |

### Running Quality Checks

```bash
# Format code
./mvnw spotless:apply

# Run all quality checks
./mvnw verify

# Run mutation tests
./mvnw org.pitest:pitest-maven:mutationCoverage
```

### Pre-Commit Hooks

Install the pre-commit hook to enforce quality standards:

```bash
ln -s ../../.hooks/pre-commit .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

The hook runs code formatting and verification before each commit.

---

## Testing

### Test Strategy

The project maintains comprehensive test coverage across multiple layers:

- **Unit Tests**: Domain logic and service layer (JUnit 5)
- **Integration Tests**: Full API workflows with Testcontainers
- **Contract Tests**: API contract validation
- **Mutation Tests**: Verify test effectiveness with PITest

### Running Tests

```bash
# Run all tests
./mvnw test

# Run integration tests only
./mvnw verify -DskipUnitTests

# Run with coverage report
./mvnw verify
# View report at: target/site/jacoco/index.html

# Run mutation tests
./mvnw org.pitest:pitest-maven:mutationCoverage
# View report at: target/pit-reports/index.html
```

### Test Coverage Requirements

| Layer | Line Coverage | Branch Coverage | Mutation Score |
|-------|---------------|-----------------|----------------|
| Domain | 100% | 100% | 90%+ |
| Application Service | 100% | 100% | 90%+ |
| Adapters | 80%+ | 80%+ | N/A |

### Writing Tests

```java
// Example: Integration test with Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class TimeEntryControllerIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    
    @Test
    @WithMockJwt(firmId = "firm-123", role = "EMPLOYEE")
    void shouldCreateTimeEntry() {
        // Test implementation
    }
}
```

---

## Deployment

### Docker Deployment

#### Build Docker Image

```bash
docker build -f docker/Dockerfile -t aequitas-central-service:latest .
```

#### Run with Docker Compose

```bash
docker compose up -d
```

#### Environment Configuration

Production environment variables:

```bash
# Database Configuration
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/aequitas
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=secure_password

# Security Configuration
export SECURITY_JWT_SECRET=production-secret-min-32-chars

# Supabase Authentication
export SUPABASE_URL=https://your-project.supabase.co
export SUPABASE_KEY=your-supabase-anon-key
export SUPABASE_JWT_SECRET=your-supabase-jwt-secret

# Application Profile
export SPRING_PROFILES_ACTIVE=prod
```

### Production Checklist

- [ ] Set secure `SECURITY_JWT_SECRET` (minimum 32 characters)
- [ ] Configure Supabase project with production credentials
- [ ] Configure production database with SSL
- [ ] Enable HTTPS/TLS termination at load balancer
- [ ] Configure log aggregation (ELK, Splunk, CloudWatch)
- [ ] Set up monitoring dashboards (Prometheus + Grafana)
- [ ] Configure backup strategy for PostgreSQL
- [ ] Review and apply security patches
- [ ] Set appropriate JVM heap size (`-Xmx`, `-Xms`)
- [ ] Configure health check endpoints in orchestrator
- [ ] Set up alerts for critical metrics
- [ ] Configure rate limiting for authentication endpoints
- [ ] Set up email verification for user registration (Supabase)

### Health Checks

```bash
# Liveness probe
curl http://localhost:8080/actuator/health/liveness

# Readiness probe
curl http://localhost:8080/actuator/health/readiness
```

### Database Migration

Flyway runs automatically on startup. For production rollouts:

```bash
# Validate migrations
./mvnw flyway:validate

# Preview pending migrations
./mvnw flyway:info

# Apply migrations manually
./mvnw flyway:migrate
```

---

## Observability

### Health Endpoints

```bash
# Overall health status
GET /actuator/health

# Detailed health checks
GET /actuator/health/liveness
GET /actuator/health/readiness
GET /actuator/health/db
```

### Metrics

Prometheus-compatible metrics available at:

```bash
GET /actuator/prometheus
GET /actuator/metrics
```

#### Key Metrics

- `http_server_requests_seconds`: API latency percentiles
- `outbox_relay_published_total`: Events published successfully
- `outbox_relay_failed_total`: Failed event publications
- `idempotency_cache_hits_total`: Idempotency cache effectiveness
- `jvm_memory_used_bytes`: JVM memory utilization
- `hikaricp_connections_active`: Database connection pool usage

### Logging

Structured JSON logging with correlation IDs:

```json
{
  "timestamp": "2025-11-09T12:34:56.789Z",
  "level": "INFO",
  "correlationId": "abc-123-def-456",
  "firmId": "firm-uuid",
  "userId": "user-uuid",
  "message": "Time entry created",
  "entryId": "entry-uuid"
}
```

#### Log Levels

Configure via environment:

```bash
export LOGGING_LEVEL_COM_AEQUITAS=DEBUG
```

### Distributed Tracing

Correlation IDs propagate across service boundaries via `X-Correlation-Id` header.

---

## Troubleshooting

### Common Issues

#### Issue: `current_setting` errors for missing GUC

**Symptom**: PostgreSQL errors about missing `app.current_firm_id` or `app.current_user_id`

**Resolution**: 
- Ensure requests include valid JWT with `firm_id` and `sub` claims
- For background jobs, provide service token or temporarily disable RLS
- Check `TenantContextFilter` is properly configured

#### Issue: Maven build fails with dependency errors

**Symptom**: `mvn verify` cannot resolve dependencies or parent POM

**Resolution**:
```bash
# Use local repository
./mvnw -Dmaven.repo.local=.m2 clean install

# Pre-populate dependencies
./mvnw -Dmaven.repo.local=.m2 dependency:go-offline
```

#### Issue: 401 Unauthorized during testing

**Symptom**: API requests return 401 even with Bearer token

**Resolution**:
- Verify JWT secret matches between token generation and application config
- Use `@WithMockJwt` annotation in tests
- Check token expiration and required claims (`sub`, `firm_id`, `role`)

#### Issue: User registration fails with 404 Firm not found

**Symptom**: POST to `/api/v1/auth/signup` returns 404 error

**Resolution**:
- Ensure the `firmId` in the signup request corresponds to an existing firm
- Create the firm first using the `/api/v1/firms` endpoint (requires SYSTEM_ADMIN role)
- Verify the firm UUID is correctly formatted

#### Issue: Authentication fails with 409 User already exists

**Symptom**: POST to `/api/v1/auth/signup` returns 409 conflict error

**Resolution**:
- The email address is already registered in the system
- Use `/api/v1/auth/signin` to authenticate with existing credentials
- If password is forgotten, implement password reset flow through Supabase

#### Issue: Invalid token claims or missing role

**Symptom**: JWT token is valid but authorization fails for specific endpoints

**Resolution**:
- Verify the JWT token includes all required claims: `sub`, `firm_id`, and `role`
- Check that the `role` value matches one of: `EMPLOYEE`, `MANAGER`, `ADMIN`
- Ensure the user's role assignment in the database matches the token claims
- Re-authenticate to obtain a fresh token with updated claims

#### Issue: Docker container fails health checks

**Symptom**: Container repeatedly restarts or shows unhealthy status

**Resolution**:
```bash
# Check container logs
docker logs aequitas-central-service

# Verify database connectivity
docker exec -it aequitas-postgres psql -U aequitas -d aequitas_central

# Check application health
curl http://localhost:8080/actuator/health
```

#### Issue: Flyway migration conflicts

**Symptom**: `FlywayException` about checksum mismatches or out-of-order migrations

**Resolution**:
```bash
# Repair Flyway schema history
./mvnw flyway:repair

# For development only: Clean and rebuild
./mvnw flyway:clean flyway:migrate
```

### Debug Mode

Enable debug logging:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--logging.level.com.aequitas=DEBUG
```

### Performance Troubleshooting

```bash
# Check database connection pool
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active

# Monitor JVM memory
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Check slow queries (PostgreSQL)
docker exec -it aequitas-postgres psql -U aequitas -c "SELECT * FROM pg_stat_statements ORDER BY total_time DESC LIMIT 10;"
```

---

## Contributing

We welcome contributions from the community! Please follow these guidelines:

### Development Process

1. **Fork the repository** and create a feature branch
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes** following code standards
   - Use Google Java Style formatting
   - Write comprehensive tests (maintain 100% coverage for domain/service layers)
   - Add Javadoc for public APIs

3. **Run quality checks** before committing
   ```bash
   ./mvnw spotless:apply
   ./mvnw verify
   ```

4. **Commit with meaningful messages**
   ```bash
   git commit -m "feat: Add customer project validation"
   ```

5. **Push and create Pull Request**
   ```bash
   git push origin feature/your-feature-name
   ```

### Code Standards

- Follow hexagonal architecture patterns
- Maintain immutability in domain objects
- Use constructor injection for dependencies
- Write integration tests for new API endpoints
- Update `docs/requirements-matrix.md` for new features

### Pull Request Checklist

- [ ] All tests pass (`./mvnw verify`)
- [ ] Code coverage maintained (100% domain/service)
- [ ] Mutation tests pass (90%+ score)
- [ ] Javadoc added for public APIs
- [ ] README updated (if applicable)
- [ ] Requirements matrix updated (if applicable)
- [ ] Changelog entry added

### Documentation

See additional documentation:
- `docs/requirements-matrix.md` - Feature traceability
- `documentation/plantUML/` - Architecture diagrams
- `documentation/markdown/milestone1_LLD.md` - Low-level design

---

## License

This project is licensed under the **Apache License 2.0**.

```
Copyright 2025 Aequitas Engineering

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

See the [LICENSE](LICENSE) file for full details.

---

## Changelog

### [0.1.0-SNAPSHOT] - Current Development

#### Added
- Multi-tenant time entry management with RLS
- Role-based access control (Employee, Manager, Admin)
- Idempotent API operations with 24-hour deduplication
- Transactional outbox pattern for event publishing
- OAuth2/JWT authentication with claim propagation
- Comprehensive test suite with 100% domain coverage
- Docker-based deployment with distroless containers
- Prometheus metrics and structured logging
- OpenAPI/Swagger documentation
- Flyway database migrations

#### Security
- Row-level security policies for tenant isolation
- JWT-based authentication and authorization
- Encrypted integration credentials storage

---

## Support

For questions, issues, or feature requests:

- **Issues**: [GitHub Issues](https://github.com/EricNTaurone/AequitasCentralService/issues)
- **Documentation**: See `docs/` directory
- **Architecture**: Review PlantUML diagrams in `documentation/plantUML/`

---

## Acknowledgments

Built with:
- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [PostgreSQL](https://www.postgresql.org/) - Database with RLS support
- [Flyway](https://flywaydb.org/) - Database migrations
- [Testcontainers](https://www.testcontainers.org/) - Integration testing
- [Micrometer](https://micrometer.io/) - Metrics collection
- [Springdoc OpenAPI](https://springdoc.org/) - API documentation

---

**Made with ⚖️ by Aequitas Engineering**
