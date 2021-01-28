# syntax=docker/dockerfile:experimental

## Build inside docker with a maven container
FROM maven:3.6.3-jdk-11-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -f pom.xml clean package

## Copy isolated build from intermediate container(useful on server)
FROM payara/micro AS prod
## Fixes clock in payara log
ENV TZ Europe/Oslo
COPY --from=build /app/target/ctscanarkivsystemserver-0.0.1-SNAPSHOT.war $DEPLOY_DIR/ctscanarkivsystemserver-0.0.1-SNAPSHOT.war
RUN mkdir /opt/payara/images
RUN chown payara:payara /opt/payara/images
USER payara


## Local build from IDE; only copy (no docker build) from exising .war file built by IDE
FROM payara/micro AS run-only
ENV TZ Europe/Oslo
COPY ./target/ctscanarkivsystemserver-0.0.1-SNAPSHOT.war $DEPLOY_DIR/ctscanarkivsystemserver-0.0.1-SNAPSHOT.war
