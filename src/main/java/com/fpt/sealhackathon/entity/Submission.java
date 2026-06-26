package com.fpt.sealhackathon.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Submission {
    @Id
    private UUID id;

    @Column(name = "round_participant_id")
    private UUID roundParticipantId;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "round_id")
    private UUID roundId;

    @Column(name = "round_track_id")
    private UUID roundTrackId;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "repo_url")
    private String repoUrl;

    @Column(name = "demo_url")
    private String demoUrl;

    @Column(name = "slide_url")
    private String slideUrl;

    @Column(name = "report_url")
    private String reportUrl;

    @Column(name = "api_metadata")
    private String apiMetadata;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status")
    private SubmissionStatus status;
}
