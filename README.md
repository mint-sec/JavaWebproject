# Campus Time Bank

Campus Time Bank is a Spring Boot backend scaffold for a campus skill-sharing platform.

## Tech Stack

- Java 11
- Spring Boot 2.7
- MyBatis-Plus
- MySQL 8
- Redis
- Maven

## Modules

- User: registration, profile, wallet balance
- Skill: publish and browse skill services
- Order: create, freeze, complete, cancel
- Review: evaluate completed services
- Wallet: track time-coin flow

## Quick Start

1. Create a MySQL database named `campus_time_bank`.
2. Run the SQL in `src/main/resources/schema.sql`.
3. Update datasource settings in `src/main/resources/application-dev.yml`.
4. Start the project with `mvn spring-boot:run`.

## API Overview

- `GET /api/health`
- `POST /api/users`
- `GET /api/users`
- `POST /api/skills`
- `GET /api/skills`
- `POST /api/orders`
- `POST /api/orders/{id}/complete`
- `POST /api/orders/{id}/cancel`
- `POST /api/reviews`

## Next Steps

- Add login and campus identity verification
- Add order acceptance and service check-in flow
- Add Redis cache, distributed lock and WebSocket messaging
- Add admin review and dispute handling
