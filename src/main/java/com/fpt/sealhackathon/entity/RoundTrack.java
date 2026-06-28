package com.fpt.sealhackathon.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "round_tracks")
@Getter
@Setter
public class RoundTrack {

    @Id
    private UUID id;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "round_id")
    private UUID roundId;
}
