# OpenAPI Specification

This directory contains the OpenAPI 3.0 specification for the Aequitas Central Service API.

## Files

- `aequitas-api.yaml` - Complete OpenAPI 3.0 specification documenting all REST endpoints, DTOs, and operations

## Overview

The API specification documents two main resource groups:

### Time Entries API (`/api/v1/entries`)

- **POST** `/api/v1/entries` - Create a new time entry
- **GET** `/api/v1/entries` - Search time entries with filtering and pagination
- **GET** `/api/v1/entries/{id}` - Get a specific time entry
- **PATCH** `/api/v1/entries/{id}` - Update a time entry (DRAFT only)
- **POST** `/api/v1/entries/{id}/submit` - Submit entry for approval
- **POST** `/api/v1/entries/{id}/approve` - Approve a submitted entry

### User Profiles API (`/api/v1/users`)

- **GET** `/api/v1/users/me` - Get authenticated user's profile
- **GET** `/api/v1/users` - List all users (filtered by role)
- **PATCH** `/api/v1/users/{id}/role` - Update user role assignment

### Firm Details API (`/api/v1/firms`)

- **GET** `/api/v1/firms/me` - Get the authenticated user's firm
- **GET** `/api/v1/firms/{id}` - Get details associated with a particular firm
- **GET** `/api/v1/firms` - Get a paginated list of firms
- **POST** `/api/v1/firms` - Create a new firm
    \_ **PATCH** `/api/v1/firms/{id}` - Update a firm's details

## Code Generation

The OpenAPI specification is used to generate DTOs and API interfaces via the `openapi-generator-maven-plugin`.

### Generated Artifacts

Generated code is placed in `target/generated-sources/openapi/`:

**DTOs** (in `com.aequitas.aequitascentralservice.adapter.web.generated.dto`):

- `CreateTimeEntryRequest`
- `UpdateTimeEntryRequest`
- `TimeEntryResponse`
- `TimeEntryPageResponse`
- `UserProfileResponse`
- `UpdateUserRoleRequest`
- `IdResponse`
- `EntryStatus` (enum)
- `Role` (enum)
- `ErrorResponse`

**API Interfaces** (in `com.aequitas.aequitascentralservice.adapter.web.generated.api`):

- `TimeEntriesApi` - Interface defining time entry operations
- `UserProfilesApi` - Interface defining user profile operations

### Generating Code

To generate DTOs and API interfaces:

```bash
mvn generate-sources
```

Or as part of a full build:

```bash
mvn clean install
```

The generated code is automatically included in the compilation classpath.

## Plugin Configuration

The `openapi-generator-maven-plugin` is configured in `pom.xml` with the following key settings:

- **Generator**: `spring` (Spring Boot server)
- **Interface Only**: `true` (generates interfaces, not implementations)
- **Spring Boot 4**: `true`
- **Jakarta EE**: `true` (uses `jakarta.*` packages)
- **Bean Validation**: `true` (generates `@Valid`, `@NotNull`, etc.)
- **Springdoc Integration**: `true` (generates Swagger annotations)

## Validation

The generated DTOs include Jakarta Bean Validation annotations that match the OpenAPI schema constraints:

- `@NotNull` for required fields
- `@Size` for string length constraints
- `@Min`/`@Max` for numeric ranges
- `@Pattern` for regex validation

## Documentation

The API documentation is automatically available via Springdoc OpenAPI UI when the application is running:

- **Swagger UI**: <http://localhost:8080/swagger-ui.html>
- **OpenAPI JSON**: <http://localhost:8080/v3/api-docs>

## Integration with Existing Code

The existing controllers (`TimeEntryController`, `UserProfileController`) can optionally implement the generated API interfaces to ensure they conform to the OpenAPI specification:

```java
@RestController
@RequestMapping("/api/v1/entries")
public class TimeEntryController implements TimeEntriesApi {
    // Implementation...
}
```

This provides compile-time validation that the controller matches the API contract.

## Updating the Specification

When adding or modifying endpoints:

1. Edit `aequitas-api.yaml` to reflect the changes
2. Run `mvn generate-sources` to regenerate DTOs and interfaces
3. Update controllers to use the new generated types
4. Run tests to verify compatibility

## Design-First Approach

This project uses a **design-first** approach:

1. Define the API contract in OpenAPI YAML
2. Generate DTOs and interfaces from the specification
3. Implement controllers using the generated contracts
4. Validate that implementations match the specification

This ensures:

- ✅ Consistent API contracts across teams
- ✅ Automated validation of request/response formats
- ✅ Self-documenting code with Swagger UI
- ✅ Type-safe API development
- ✅ Client SDK generation capability

## Additional Resources

- [OpenAPI Specification](https://spec.openapis.org/oas/v3.0.3)
- [OpenAPI Generator Documentation](https://openapi-generator.tech/)
- [Springdoc OpenAPI](https://springdoc.org/)
