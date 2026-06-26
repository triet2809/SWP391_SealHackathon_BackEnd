package com.fpt.sealhackathon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "round_participants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoundParticipant {

    @Id
    private UUID id;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "round_id")
    private UUID roundId;

    @Column(name = "round_track_id")
    private UUID roundTrackId;

    @Column(name = "team_id")
    private UUID teamId;
}
