package Group1.Topic4.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegistRequest {
    private String email;
    private String password;
    private String fullName;
    private String studentId;
    private String studentType;
}
