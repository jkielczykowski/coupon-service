# Coupon Service

Coupon Service is a REST API for managing discount coupons.

The application allows:
- creating new coupons,
- redeeming coupons,
- validating coupon usage limits,
- validating coupon availability by country based on client IP,
- preventing duplicate coupon redemption by the same user.

## Tech stack

- Java / Spring Boot
- Gradle
- PostgreSQL
- Flyway
- Docker / Docker Compose
- OpenAPI

---

## How to run the application

### 1. Start the database first

The project contains a `docker-compose.yml` file located in the `resources` directory.

This should be started first.

Once it is started, PostgreSQL will be created and run inside Docker.

### 2. Build the application

Before starting the application, run:

```bash
./gradlew clean build