package Group1.Topic4.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegistRequest {
    private String email;
    private String password;
    private String fullName;
    private String universityName;
    private String studentId;
}
