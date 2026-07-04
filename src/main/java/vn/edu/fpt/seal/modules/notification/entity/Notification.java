package vn.edu.fpt.seal.modules.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.fpt.seal.common.entity.BaseEntity;
import vn.edu.fpt.seal.modules.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "type", nullable = false, length = 40)
    private String type;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "body", columnDefinition = "text")
    private String body;

    /** Logical grouping mapped to a sidebar feature (submissions, join_requests, support, notices). */
    @Column(name = "category", nullable = false, length = 40)
    private String category;

    @Column(name = "ref_type", length = 40)
    private String refType;

    @Column(name = "ref_id")
    private UUID refId;

    @Column(name = "read_at")
    private LocalDateTime readAt;
}
