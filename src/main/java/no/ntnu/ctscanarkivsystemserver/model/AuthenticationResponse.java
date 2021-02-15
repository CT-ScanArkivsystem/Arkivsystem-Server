package no.ntnu.ctscanarkivsystemserver.model;

/**
 * @author koushikkothagal
 * @source https://github.com/koushikkothagal/spring-security-jwt/blob/master/src/main/java/io/javabrains/springsecurityjwt/models/AuthenticationResponse.java
 */
public class AuthenticationResponse {
    private final String jwt;

    public AuthenticationResponse(String jwt) {
        this.jwt = jwt;
    }

    public String getJwt() {
        return jwt;
    }
}
