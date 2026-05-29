package Group1.Topic4.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
@Getter
@Setter
@Entity
@Table(name = "users")
public class Users {
    @ColumnDefault("'STUDENT'")
    @JdbcTypeCode(SqlTypes.OTHER)
    @Column(name = "system_role", columnDefinition = "user_role")
    private Object systemRole;
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;
    @ColumnDefault("false")
    @Column(name = "is_approved")
    private Boolean isApproved;
    @Size(max = 255)
    @ColumnDefault("'FPT University'")
    @Column(name = "university_name")
    private String universityName;
    @Size(max = 50)
    @Column(name = "student_id", length = 50)
    private String studentId;
    @Size(max = 255)
    @NotNull
    @Column(name = "full_name", nullable = false)
    private String fullName;
    @Size(max = 255)
    @NotNull
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Size(max = 255)
    @NotNull
    @Column(name = "email", nullable = false)
    private String email;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;


}