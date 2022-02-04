# quarkus-camel-jms Project

## Startup
- Start database and artemis: `cd localdeployment && docker compose up -d`
- Start application: `./mvnw quarkus:dev`

## Cleanup
- Stop database and artemis: `cd localdeployment && docker compose down`