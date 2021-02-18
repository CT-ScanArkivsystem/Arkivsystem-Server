package no.ntnu.ctscanarkivsystemserver.api;

import no.ntnu.ctscanarkivsystemserver.model.AuthenticationRequest;
import no.ntnu.ctscanarkivsystemserver.service.UserService;
import no.ntnu.ctscanarkivsystemserver.util.JwtUtil;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * The job of this class is to be the endpoint for all authentication and authorization
 * requests which is accessible for all user. (With or without an account)
 */
@RequestMapping("/auth")
@RestController
public class AuthController {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @PostMapping(path = "/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest authenticationRequest, HttpServletResponse response) throws BadCredentialsException, UnsupportedEncodingException {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Incorrect username or password!", e);
        }
        final UserDetails userDetails = userService
                .loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtTokenUtil.generateToken(userDetails);

        Cookie cookie = new Cookie("jwt", URLEncoder.encode("Bearer " + jwt, "UTF-8"));
        //System.out.println(URLEncoder.encode(("Bearer " + jwt), "UTF-8"));
        cookie.setMaxAge(1800);
        cookie.setPath("/");
        cookie.setDomain("localhost");
        cookie.setSecure(false); // TODO: When the connection becomes secure (HTTPS) change this to true!
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        return new ResponseEntity<>("{\"success\": true}", HttpStatus.OK);
    }
}
