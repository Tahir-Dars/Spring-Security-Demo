package bank.springdemo.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String jwtToken;
    private String userName;
    private List<String> roles;

}
