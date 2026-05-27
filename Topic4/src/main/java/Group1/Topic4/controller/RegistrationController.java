package Group1.Topic4.controller;

import Group1.Topic4.Service.AuthService;
import Group1.Topic4.dto.RegistRequest;
import Group1.Topic4.dto.RegistResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistrationController {
    private final AuthService authService;

    public RegistrationController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/register")
    public ResponseEntity<RegistResponse> register(@RequestBody RegistRequest registRequest) {
        boolean success = authService.register(registRequest.getEmail(), registRequest.getPassword(),registRequest.getFullName());
        if(success){
            return ResponseEntity.ok(new RegistResponse("Registration successful"));
        }

        return ResponseEntity.ok(new RegistResponse("Registration failed"));
    }

}
