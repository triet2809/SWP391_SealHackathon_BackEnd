package vn.edu.fpt.seal.modules.round.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.fpt.seal.common.entity.BaseEntity;
import vn.edu.fpt.seal.modules.track.entity.Track;

import java.time.LocalDateTime;

@Entity
@Table(name = "rounds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Round extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Column(name = "submission_deadline", nullable = false)
    private LocalDateTime submissionDeadline;

    @Column(name = "top_n_to_promote", nullable = false)
    private Integer topNToPromote;

    /** Thời điểm EC công bố kết quả vòng thi (mở cửa sổ khiếu nại). NULL = chưa công bố. */
    @Column(name = "result_published_at")
    private LocalDateTime resultPublishedAt;

    /** Hạn chót nộp khiếu nại = resultPublishedAt + 15 phút. NULL = chưa mở cửa sổ. */
    @Column(name = "appeal_deadline")
    private LocalDateTime appealDeadline;
}
