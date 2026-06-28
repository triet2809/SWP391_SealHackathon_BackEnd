package com.fpt.sealhackathon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
@Entity
@Table(name = "rounds")
public class Round {

    @Id
    private UUID id;

    @Column(name = "event_id")
    private UUID eventId;
}
