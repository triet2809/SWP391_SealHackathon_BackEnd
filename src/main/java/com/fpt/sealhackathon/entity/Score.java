package com.fpt.sealhackathon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SecondaryRow;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "scores")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Score {
    @Id
    private UUID id;

    @Column(name = "submission_id")
    private UUID submissionId;

    @Column(name = "judge_id")
    private UUID judgeId;

    @Column(name = "criterion_id")
    private UUID criterionId;

    @Column(name = "score")
    private BigDecimal score;

    @Column(name = "weighted_score")
    private BigDecimal weightedScore;

    @Column(name = "criterion_average_score")
    private BigDecimal criterionAverageScore;

    @Column(name = "criterion_variance")
    private BigDecimal criterionVariance;

    @Column(name = "criterion_stddev")
    private BigDecimal criterionStddev;

    @Column(name = "comment")
    private String comment;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
