package vn.edu.fpt.seal.modules.ranking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import vn.edu.fpt.seal.modules.round.entity.Round;
import vn.edu.fpt.seal.modules.team.entity.Team;
import vn.edu.fpt.seal.modules.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Quyết định phân định hòa THỦ CÔNG sau khi ban giám khảo review mã nguồn GitHub.
 * Khi hai đội hòa nhau ở mọi tiêu chí tự động, EC có thể tạo quyết định cho đội thắng;
 * lần tính lại xếp hạng tiếp theo sẽ ưu tiên đội có quyết định này trong nhóm hòa.
 */
@Entity
@Table(name = "tie_break_decisions", uniqueConstraints =
        @UniqueConstraint(name = "uq_tie_break_decisions_round_team", columnNames = {"round_id", "team_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TieBreakDecision {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "round_id", nullable = false)
    private Round round;

    /** Đội được ưu tiên xếp trên trong nhóm hòa. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /** Người ra quyết định (điều phối viên). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "decided_by", nullable = false)
    private User decidedBy;

    @CreationTimestamp
    @Column(name = "decided_at", nullable = false, updatable = false)
    private LocalDateTime decidedAt;

    /** Lý do quyết định (ví dụ: chất lượng commit, lịch sử đóng góp...). */
    @Column(name = "reason", nullable = false, columnDefinition = "text")
    private String reason;

    /** Link bằng chứng (ví dụ: repo/commit GitHub được review). */
    @Column(name = "evidence_url", length = 500)
    private String evidenceUrl;

    /** Ghi chú thêm của người quyết định. */
    @Column(name = "note", columnDefinition = "text")
    private String note;
}
