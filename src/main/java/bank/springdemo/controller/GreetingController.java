package bank.springdemo.controller;

import bank.springdemo.jwt.LoginRequest;
import bank.springdemo.jwt.jwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class GreetingController {

    private AuthenticationManager authenticationManager;

    @Autowired
    private jwtUtils jwtutils;

    @GetMapping("/hello")
    public String sayhello() {
        return "Hello Brother";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/hello")
    public String sayhelloToUser() {
        return "Hello user";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/hello")
    public String sayhelloToAdmin() {
        return "Hello Admin";
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUserName(), loginRequest.getPassword()
                    )
            );
        } catch (AuthenticationException authenticationException) {
            Map<String, Object> stringObjectMap = new HashMap<>();
            stringObjectMap.put("Message: ", "Bad Credentials");
            stringObjectMap.put("Status: ", false);
            return new ResponseEntity<Object>(stringObjectMap, HttpStatus.UNAUTHORIZED);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return null;
    }
}
