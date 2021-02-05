# syntax=docker/dockerfile:experimental

## Build inside docker with a maven container
FROM maven:3.6.3-jdk-11-slim AS build
COPY pom.xml .
COPY src ./src
ADD . ./docker-spring-boot
WORKDIR /docker-spring-boot
RUN mvn clean install

## Local build from IDE; only copy (no docker build) from exising .war file built by IDE
FROM tomcat:10.0-jdk11 AS run-only
ENV TZ Europe/Oslo
ADD ./target/ctscanarkivsystemserver-0.0.1-SNAPSHOT.war webapps/ctscanarkivsystemserver-0.0.1-SNAPSHOT.war
ENTRYPOINT ["java","-jar", "webapps/ctscanarkivsystemserver-0.0.1-SNAPSHOT.war"]
