package vn.edu.fpt.seal.modules.teamtimeline.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import vn.edu.fpt.seal.common.enums.TimelineEventType;
import vn.edu.fpt.seal.modules.event.entity.Event;
import vn.edu.fpt.seal.modules.round.entity.Round;
import vn.edu.fpt.seal.modules.team.entity.Team;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Một mốc trên "hành trình" (timeline) của đội thi trong một sự kiện.
 * Mỗi hành động quan trọng (tạo đội, bị loại, thăng hạng, khiếu nại, giải thưởng...)
 * sẽ ghi thêm một dòng vào bảng này — chỉ ghi thêm (append-only), không sửa/xóa.
 */
@Entity
@Table(name = "team_timeline_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeamTimelineEvent {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    /** Sự kiện (hackathon) mà mốc này thuộc về — suy ra từ team.track.event khi ghi. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    /** Đội thi liên quan tới mốc này. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /** Vòng thi liên quan (nullable — ví dụ mốc "tạo đội" không gắn với vòng nào). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id")
    private Round round;

    /** Loại mốc — lưu VARCHAR để dễ thêm loại mới không cần đổi pg enum. */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    private TimelineEventType type;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    /** Ảnh chụp (snapshot) điểm số tại thời điểm ghi mốc — nullable. */
    @Column(name = "score_snapshot", precision = 10, scale = 2)
    private BigDecimal scoreSnapshot;

    /** Ảnh chụp thứ hạng tại thời điểm ghi mốc — nullable. */
    @Column(name = "rank_snapshot")
    private Integer rankSnapshot;

    /** Ảnh chụp trạng thái (ví dụ promoted/eliminated/disqualified) — nullable. */
    @Column(name = "status_snapshot", length = 40)
    private String statusSnapshot;

    /** Thời điểm mốc xảy ra (tự sinh khi insert). */
    @CreationTimestamp
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private LocalDateTime occurredAt;
}
