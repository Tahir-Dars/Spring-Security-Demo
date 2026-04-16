package bank.springdemo.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private final jwtUtils jwtUtilss;
    private final UserDetailsService userDetailsService;
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    public AuthTokenFilter(jwtUtils jwtUtilss, UserDetailsService userDetailsService) {
        this.jwtUtilss = jwtUtilss;
        this.userDetailsService = userDetailsService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        logger.debug("AuthTokenFilter URI:{}", request.getRequestURI());
        try {
            String jwt = parsejwt(request);
            if (!jwt.isEmpty() && jwtUtilss.validateJwtToken(jwt)) {
                String userName = jwtUtilss.getUserNameFromJwtToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                logger.debug("Roles from JWT:{}", userDetails);
            }
        } catch (Exception exception) {
            logger.debug("Can't set user authentication: {}", exception.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private String parsejwt(HttpServletRequest request) {
        String jwt = jwtUtilss.getJwt_FromHeader(request);
        logger.debug("AuthTokenFilter.java : {}", jwt);
        return jwt;
    }
}
