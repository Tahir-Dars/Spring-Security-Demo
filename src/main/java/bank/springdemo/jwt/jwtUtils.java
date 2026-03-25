package bank.springdemo.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class jwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(jwtUtils.class);
    private int jwtExpirations_ms;
    private String jwtSecret;

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
        return Jwts.builder().subject(userName)
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + jwtExpirations_ms))
                .signWith(key()) //{New method in the class will be created for this}
                .compact();
    }

    //Getting the Username from JWT Token
    public String getUserNameFromJwtToken(String jwtToken) {
        return Jwts.parser().verifyWith((SecretKey) key())
                .build().parseSignedClaims(jwtToken)
                .getPayload().getSubject();
    }

    //Generating Signing key ^
    public Key key() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );

    }
}
