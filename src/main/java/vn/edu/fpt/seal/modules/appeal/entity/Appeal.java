package vn.edu.fpt.seal.modules.appeal.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import vn.edu.fpt.seal.common.enums.AppealStatus;
import vn.edu.fpt.seal.modules.event.entity.Event;
import vn.edu.fpt.seal.modules.round.entity.Round;
import vn.edu.fpt.seal.modules.team.entity.Team;
import vn.edu.fpt.seal.modules.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Đơn khiếu nại kết quả của một vòng thi.
 * Quy tắc nghiệp vụ: chỉ được nộp trong vòng 15 phút kể từ khi EC công bố kết quả
 * (appealDeadline được snapshot lại từ round tại thời điểm nộp để phục vụ audit).
 */
@Entity
@Table(name = "appeals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Appeal {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "round_id", nullable = false)
    private Round round;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /** Người nộp đơn (leader hoặc member của đội). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    @Column(name = "reason", nullable = false, columnDefinition = "text")
    private String reason;

    /** Trạng thái xử lý — lưu VARCHAR (PENDING/ACCEPTED/REJECTED). */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AppealStatus status = AppealStatus.PENDING;

    /** Phản hồi của điều phối viên. */
    @Column(name = "response", columnDefinition = "text")
    private String response;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    /** Snapshot thời điểm công bố kết quả của vòng (lấy từ round lúc nộp đơn). */
    @Column(name = "result_published_at")
    private LocalDateTime resultPublishedAt;

    /** Snapshot hạn chót khiếu nại (lấy từ round lúc nộp đơn). */
    @Column(name = "appeal_deadline")
    private LocalDateTime appealDeadline;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
