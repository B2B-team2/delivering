# Project Instructions (Delivering)

## Architecture & Infrastructure
- **Gateway Port**: 8080 (Primary entry point for all API requests).
- **Service Discovery**: Eureka Server (8761).
- **Configuration**: Config Server (8888) using `native` profile (reading from `config-server/src/main/resources/config`).
- **Security**: Spring Security is currently in **Temporary Bypass Mode** (Permit All) across all services for development/testing.

## API Documentation Standards
- **Springdoc OpenAPI**: 
  - Every microservice must include `springdoc-openapi-starter-webmvc-ui`.
  - API Gateway includes `springdoc-openapi-starter-webflux-ui` for aggregation.
  - Gateway aggregated Swagger path: `/swagger-ui/index.html`.
- **Postman**: 
  - Shared workspace: `jojo Workspace`.
  - All collection request URLs must point to the Gateway (8080) with appropriate service prefixes.

## AI Automated Tasks & Rules
1. **Adding New Service**:
   - Always add `spring-boot-starter-security` and the `SecurityConfig` bypass class.
   - Add `springdoc-openapi-starter-webmvc-ui`.
   - Update `api-gateway.yml` in Config Server to include the new service's Swagger URL.
2. **Postman Updates**:
   - When updating or creating Postman requests, ensure the base URL is `http://localhost:8080/{service-prefix}`.
   - Use the OpenAPI JSON URL (`http://localhost:8080/{service-prefix}/v3/api-docs`) to import or sync collections directly in Postman.
3. **Build Validation**:
   - After any change to `build.gradle` or configuration classes, run `./gradlew :<module-name>:classes` to verify the build.
