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

@Entity
@Table(name = "track_mentors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class TrackMentor {
    @Id
    private UUID id;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "round_track_id")
    private UUID roundTrackId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
}
