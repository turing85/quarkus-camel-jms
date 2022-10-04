# quarkus-camel-jms Project

## Startup
- Start database and artemis: `cd local-deployment && docker compose up -d && cd ..`
- Start applications (in two different terminals):
  - 1st terminal: `./mvnw -f sender quarkus:dev`
  - 2nd terminal: `./mvnw -f receiver quarkus:dev`

## Error case
- Stop the database: `cd local-deployment && docker compose stop postgres && cd ..`
- Observe that the next request fails, and that the route then does nothing for 10 seconds
- The next request will fail again, and the route will then again do nothing for 10 seconds
- restart the database: `cd local-deployment && docker compose up postgres -d && cd ..`
- Observe that the route will start processing again

## Cleanup
- Stop database and artemis: `cd local-deployment && docker compose down && cd`