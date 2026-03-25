package bank.springdemo.jwt;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class jwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(jwtUtils.class);
    private int jwtExpirations_ms;

    //Getting tokens from the headers
    public String getJwt_FromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        logger.debug("Authorization Header:{}", bearerToken);
        if (bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    //Generating the tokens using the username
    public String genTokensFromUserName(UserDetails userDetails) {
        String userName = userDetails.getUsername();
        String jwtToken = Jwts.builder().subject(userName)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + jwtExpirations_ms))
                .signWith(Key()) //{New method in the class will be created for this}
                .compact();
        return jwtToken;
    }
}
