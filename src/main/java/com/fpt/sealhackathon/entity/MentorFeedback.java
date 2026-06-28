package com.fpt.sealhackathon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "mentor_feedbacks")

public class MentorFeedback {
    @Id
    private UUID id;

    @Column(name = "mentor_id")
    private UUID mentorId;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "round_id")
    private UUID roundId;

    @Column(name = "round_track_id")
    private UUID roundTrackId;

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "content")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
