package vn.edu.fpt.seal.modules.rules.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.fpt.seal.common.entity.BaseEntity;
import vn.edu.fpt.seal.common.enums.RuleVisibility;
import vn.edu.fpt.seal.modules.event.entity.Event;

/**
 * Một điều luật của sự kiện. Mức hiển thị quyết định ai xem được:
 * - PUBLIC: thí sinh xem được (và phải chấp nhận khi đăng ký).
 * - INTERNAL / DISPUTE_ONLY: KHÔNG BAO GIỜ trả về cho thí sinh, chỉ điều phối viên.
 */
@Entity
@Table(name = "event_rules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventRule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    /** Mức hiển thị — lưu VARCHAR (PUBLIC/INTERNAL/DISPUTE_ONLY). */
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    @Builder.Default
    private RuleVisibility visibility = RuleVisibility.PUBLIC;

    /** Thứ tự hiển thị trên UI (nhỏ hơn hiển thị trước). */
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;
}
