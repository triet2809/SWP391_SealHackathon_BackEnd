package com.fpt.sealhackathon.entity;

import com.fpt.sealhackathon.dto.enums.RegistrationMode;
import com.fpt.sealhackathon.dto.enums.TeamStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "teams",
        indexes = {
                @Index(name = "idx_teams_event",   columnList = "event_id"),
                @Index(name = "idx_teams_profile", columnList = "team_profile_id"),
                @Index(name = "idx_teams_status",  columnList = "status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Team {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false, updatable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_profile_id", nullable = false, updatable = false)
    private TeamProfile teamProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", updatable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "copied_from_team_id", updatable = false)
    private Team copiedFromTeam;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "team_status", updatable = false)
    @Builder.Default
    private TeamStatus status = TeamStatus.waiting_for_members;

    @Column(name = "registration_mode", nullable = false,
            columnDefinition = "registration_mode", updatable = false)
    @Builder.Default
    private RegistrationMode registrationMode = RegistrationMode.new_team;

    @Column(name = "approved_at", insertable = false, updatable = false)
    private Instant approvedAt;

    @Column(name = "locked_at", insertable = false, updatable = false)
    private Instant lockedAt;

    @Column(name = "locked_reason", insertable = false, updatable = false)
    private String lockedReason;

    @Column(name = "eliminated_at")
    private Instant eliminatedAt;

    @Column(name = "eliminated_reason")
    private String eliminatedReason;

    @Column(name = "disqualified_reason")
    private String disqualifiedReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    @Builder.Default
    private List<TeamMember> members = new ArrayList<>();

    public boolean isLocked() {
        return this.lockedAt != null;
    }

    public boolean isTerminated() {
        return this.status == TeamStatus.eliminated
                || this.status == TeamStatus.disqualified;
    }
}