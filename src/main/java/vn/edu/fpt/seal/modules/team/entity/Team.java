package vn.edu.fpt.seal.modules.team.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import vn.edu.fpt.seal.common.entity.BaseEntity;
import vn.edu.fpt.seal.common.enums.TeamStatus;
import vn.edu.fpt.seal.modules.track.entity.Track;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "team_status")
    @Builder.Default
    private TeamStatus status = TeamStatus.active;

    @Column(name = "disqualified_reason", columnDefinition = "text")
    private String disqualifiedReason;
}
