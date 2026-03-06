package bank.springdemo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

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
}
