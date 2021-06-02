# Arkivsystem-Server
Backend of the project. This is a web server using rest-api requests to speak to a web aplication

## License
GPL

## How to set up application
1. Install git, Docker and Docker-compose on ubuntu WM.
2. Use command ```sudo git clone --branch [bracnh name] [repo link]``` to clone the project.
3. Go into application.properties and change:
    1. The IP in ```spring.datasource.url=``` must be:
        1. ```database``` (name of the database) if the database and application is running in a container either on WM or locally.
        2. The IP of the WM if application is running in the editor (Not using docker) while the database is running in a docker container on a WM.
        3. ```localhost``` if the database is running locally, but the application is running in the editor.
    2. Set ```file.user=``` to the username of the sambashare file server.
    3. Set ```file.pass=``` to the password of the sambashare file server.
    4. Set ```file.domain=``` to the IP/domain of the sambashare file server.
    5. Set ```file.url=``` to the url used to connect to the sambashare file server.
        1. Example: ```smb://IP/folderName```.
    6. Set ```prop.domain=``` and ```prop.port=``` to the IP/domain and port the frontend is using.
    7. Set ```jasypt.encryptor.password=``` to the encryption key (The password used to encrypt all fields using ```ENC()```). To encrypt text use the class JasyptPasswordEncryptor.
4. To run application:
   1. On ubuntu WM:
        1. ```sudo docker-compose -f [name of docker-compose file] build``` to build the project.
        2. ```sudo docker-compose -f [name of docker-compose file] up``` to run the application.
        3. To only run one specific container run ```sudo docker-compose -f [name of docker-compose file] up -d [name of container]``` instead.
    2. With IntelliJ Services:
        1. Go into Run/Debug Configurations --> + --> Find Docker --> Open menu --> Click on Docker-Compose.
        2. Set Compose files to the yml script in the project.
        3. Set Environment variables to ```COMPOSE_DOCKER_CLI_BUILD=1;DOCKER_BUILDKIT=1```.
        4. Set Services to ```api, database, reverse-proxy,``` or remove containers you don't want to run.
        5. You can start the Docker containers in Services found on the bottom of IntelliJ and click on Deploy.
    3. Without Docker (The database still have to run on docker):
        1. Go into the class CtScanArkivsystemServerApplication and click on Run.
