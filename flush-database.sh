#!/bin/sh
export COMPOSE_DOCKER_CLI_BUILD=1
export DOCKER_BUILDKIT=1 
export COMPOSE_DEV_FILE="docker-compose-dev.yml"

echo "Stopping docker compose $COMPOSE_DEV_FILE"
docker-compose -f $COMPOSE_DEV_FILE down --remove-orphans &&
docker-compose -f $COMPOSE_DEV_FILE rm &&
docker volume rm CT-Scan_Arkiv_database-data &&
docker-compose -f $COMPOSE_DEV_FILE up -d --build database api 
echo "The was databse and api was restarted, use CTRL-C to detach from the log console"
echo "BØØØØØ"
sleep 1.5
docker-compose -f $COMPOSE_DEV_FILE logs -f
