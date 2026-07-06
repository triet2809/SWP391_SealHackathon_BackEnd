package vn.edu.fpt.seal.modules.prize.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import vn.edu.fpt.seal.common.enums.PrizeRevisionAction;
import vn.edu.fpt.seal.modules.team.entity.Team;
import vn.edu.fpt.seal.modules.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lịch sử chỉnh sửa giải thưởng (thu hồi / chuyển giải) — append-only,
 * KHÔNG BAO GIỜ xóa cứng để đảm bảo minh bạch khi có tranh chấp.
 */
@Entity
@Table(name = "prize_revisions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PrizeRevision {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prize_id", nullable = false)
    private Prize prize;

    /** REVOKED (thu hồi) hoặc REASSIGNED (chuyển đội) — lưu VARCHAR. */
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private PrizeRevisionAction action;

    /** Đội đang giữ giải trước khi chỉnh sửa (nullable nếu giải chưa gán đội). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_team_id")
    private Team oldTeam;

    /** Đội nhận giải sau chỉnh sửa (NULL khi thu hồi). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_team_id")
    private Team newTeam;

    @Column(name = "reason", nullable = false, columnDefinition = "text")
    private String reason;

    /** Ghi chú bằng chứng (link biên bản, kết quả điều tra...). */
    @Column(name = "evidence_note", columnDefinition = "text")
    private String evidenceNote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @CreationTimestamp
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;
}
