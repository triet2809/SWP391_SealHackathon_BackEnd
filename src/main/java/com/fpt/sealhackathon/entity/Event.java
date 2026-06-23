package com.fpt.sealhackathon.entity;

import com.fpt.sealhackathon.entity.enums.EventStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "season_name", length = 50)
    private String seasonName;

    @Column(name = "season_year")
    private Integer seasonYear;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "status", nullable = false, columnDefinition = "event_status")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private EventStatus status;

    @Column(name = "registration_start_at")
    private LocalDateTime registrationStartAt;

    @Column(name = "registration_end_at")
    private LocalDateTime registrationEndAt;

    @Column(name = "registration_closed_at")
    private LocalDateTime registrationClosedAt;

    @Column(name = "min_team_size", nullable = false)
    private Integer minTeamSize;

    @Column(name = "max_team_size", nullable = false)
    private Integer maxTeamSize;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = EventStatus.draft;
        }
        if (this.minTeamSize == null) {
            this.minTeamSize = 3;
        }
        if (this.maxTeamSize == null) {
            this.maxTeamSize = 5;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
