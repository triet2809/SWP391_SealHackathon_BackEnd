package vn.edu.fpt.seal.modules.rules.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import vn.edu.fpt.seal.modules.event.entity.Event;
import vn.edu.fpt.seal.modules.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Ghi nhận việc một người dùng đã chấp nhận bộ luật (PUBLIC) của một sự kiện.
 * Mỗi cặp (user, event) chỉ có một bản ghi — đủ để chứng minh khi có tranh chấp.
 */
@Entity
@Table(name = "rule_acceptances", uniqueConstraints =
        @UniqueConstraint(name = "uq_rule_acceptances_user_event", columnNames = {"user_id", "event_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RuleAcceptance {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    /** Thời điểm người dùng tick "đồng ý với thể lệ". */
    @CreationTimestamp
    @Column(name = "accepted_at", nullable = false, updatable = false)
    private LocalDateTime acceptedAt;
}
