package no.ntnu.ctscanarkivsystemserver.exception;

/**
 * This should be thrown if a new user tries to use an email which already exists in the database.
 * @author eustimenko
 * @source https://github.com/eustimenko/spring-web-backend-demo/blob/master/logic/src/main/java/com/spring/web/demo/logic/exception/EmailExistsException.java
 */
public class ProjectNameExistsException extends RuntimeException {
    public ProjectNameExistsException(String name) {
        super("The project name: " + name + " already exists in the database!");
    }
}
