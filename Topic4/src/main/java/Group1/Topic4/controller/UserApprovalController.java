package Group1.Topic4.controller;

import Group1.Topic4.Service.AuthService;
import Group1.Topic4.entity.Users;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/approval")
public class UserApprovalController {
    private final AuthService authService;

    public UserApprovalController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Users>> getPendingUsers() {

        return ResponseEntity.ok(
                authService.getPendingUsers()
        );
    }
    @PutMapping("/approve/{id}")
    public ResponseEntity<String> approveUser(
            @PathVariable UUID id) {

        if(authService.approveUser(id)) {
            return ResponseEntity.ok("Approved");
        }

        return ResponseEntity.badRequest()
                .body("User not found");
    }
    @PutMapping("/reject/{id}")
    public ResponseEntity<String> rejectUser(
            @PathVariable UUID id) {

        if(authService.rejectUser(id)) {
            return ResponseEntity.ok("Rejected");
        }

        return ResponseEntity.badRequest()
                .body("User not found");
    }
}
