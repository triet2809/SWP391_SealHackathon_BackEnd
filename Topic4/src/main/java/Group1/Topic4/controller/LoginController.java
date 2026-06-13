package Group1.Topic4.controller;

import Group1.Topic4.Service.AuthService;
import Group1.Topic4.dto.LoginRequest;
import Group1.Topic4.dto.LoginResponse;
import Group1.Topic4.entity.Users;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class LoginController {
    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        String token = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        if (token != null) {

            Users user =
                    authService.findByEmail(
                            loginRequest.getEmail()
                    );

            return ResponseEntity.ok(
                    new LoginResponse(
                            token,
                            user.getSystemRole(),
                            "Login successful"
                    )
            );
        }


        // Logic đăng nhập sẽ được triển khai ở đây
        return ResponseEntity.ok(
                new LoginResponse(
                        null,
                        null,
                        "Login failed"
                )
        );

    }
}
