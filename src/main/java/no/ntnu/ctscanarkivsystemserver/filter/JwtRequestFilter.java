package no.ntnu.ctscanarkivsystemserver.filter;

import no.ntnu.ctscanarkivsystemserver.service.UserService;
import no.ntnu.ctscanarkivsystemserver.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;


/**
 * This class acts as the first filter in the chain to look in the header for the Bearer token.
 * If found this class will check if it is valid.
 *
 * @author koushikkothagal
 * @source https://github.com/koushikkothagal/spring-security-jwt/blob/master/src/main/java/io/javabrains/springsecurityjwt/filters/JwtRequestFilter.java
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        //System.out.println("Cookie from request: " + request.getHeader("Cookie"));
        if (request.getHeader("Cookie") != "" && request.getHeader("Cookie") != null) {
            //System.out.println("Decoded cookie: " + URLDecoder.decode(request.getHeader("Cookie"), "UTF-8"));
            final String authorizationHeader = URLDecoder.decode(request.getHeader("Cookie"), "UTF-8");
            //System.out.println("Starts with test: " + authorizationHeader.startsWith("Bearer ", 4));

            String username = null;
            String jwt = null;

            //System.out.println("AuthorizationHeader " + authorizationHeader);
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ", 4)) {
                jwt = authorizationHeader.substring(11);
                //System.out.println("Remade jwt thigny: " + jwt);
                username = jwtUtil.extractUsername(jwt);
                System.out.println("Inside auth header!!!!");
            }


            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                System.out.println("Loading user!\n" + "Name: " + userDetails.getUsername() + "\n" + userDetails.getAuthorities());

                if (jwtUtil.validateToken(jwt, userDetails)) {

                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }
        }
        chain.doFilter(request, response);
    }

}