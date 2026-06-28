package com.fpt.sealhackathon.entity;

import com.fpt.sealhackathon.dto.enums.TeamMemberRole;
import com.fpt.sealhackathon.dto.enums.TeamMemberStatus;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Maps {@code public.team_members} — a user's membership within an event-scoped team.
 *
 * <p>Trigger notes:
 * <ul>
 *   <li>BEFORE trigger {@code trg_team_members_enforce_rules} stamps
 *       {@code acceptedAt/declinedAt/removedAt} when the app sends NULL.</li>
 *   <li>AFTER trigger {@code trg_team_members_after_changed} calls
 *       {@code sync_team_status()} — call {@code entityManager.refresh(team)} after saving.</li>
 * </ul>
 *
 * <p>Partial unique index {@code uq_team_members_one_active_leader}:
 * {@code UNIQUE (team_id) WHERE role='leader' AND status IN ('invited','accepted')}.
 * Not expressible in JPA — catch {@code DataIntegrityViolationException} at service layer.
 */
@Entity
@Table(
        name = "team_members",
        indexes = {
                @Index(name = "idx_team_members_team",   columnList = "team_id"),
                @Index(name = "idx_team_members_user",   columnList = "user_id"),
                @Index(name = "idx_team_members_status", columnList = "status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMember {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false, updatable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "copied_from_team_member_id", updatable = false)
    private TeamMember copiedFromTeamMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, columnDefinition = "team_member_role")
    @Builder.Default
    private TeamMemberRole role = TeamMemberRole.member;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "team_member_status")
    @Builder.Default
    private TeamMemberStatus status = TeamMemberStatus.invited;

    @Column(name = "joined_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant joinedAt = Instant.now();

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "declined_at")
    private Instant declinedAt;

    @Column(name = "removed_at")
    private Instant removedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    public boolean isActive() {
        return this.status == TeamMemberStatus.invited
                || this.status == TeamMemberStatus.accepted;
    }

    public boolean isLeader() {
        return this.role == TeamMemberRole.leader;
    }

    public boolean isActiveLeader() {
        return isLeader() && isActive();
    }
}