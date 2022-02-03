# quarkus-camel-jms Project

## Startup
- Start database and artemis: `cd localdeployment && docker compose up -d`
- Migrate database: `cd .. && ./mvnw flyway:migrate`
- Start application: `./mvnw quarkus:dev`