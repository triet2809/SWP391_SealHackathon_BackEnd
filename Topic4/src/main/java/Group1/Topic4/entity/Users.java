package Group1.Topic4.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;

    @Size(max = 255)
    @NotNull
    @Column(name = "email", nullable = false)
    private String email;

    @Size(max = 255)
    @NotNull
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Size(max = 255)
    @NotNull
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @JdbcTypeCode(SqlTypes.OTHER)
    @Column(name = "student_type", columnDefinition = "student_type")
    private Object studentType;

    @Size(max = 100)
    @Column(name = "student_id")
    private String studentId;

    @Column(name = "campus_id")
    private UUID campusId;

    @Column(name = "is_guest")
    private Boolean isGuest;

    @JdbcTypeCode(SqlTypes.OTHER)
    @Column(name = "status", columnDefinition = "account_status")
    private Object status;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}