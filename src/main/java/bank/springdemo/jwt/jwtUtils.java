package bank.springdemo.jwt;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class jwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(jwtUtils.class);

    //Getting jwt from tokens
    public String getJwt_FromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        logger.debug("Authorization Header:{}", bearerToken);
        if (bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
