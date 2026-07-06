package vn.edu.fpt.seal.modules.appeal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.seal.common.enums.AppealStatus;
import vn.edu.fpt.seal.modules.appeal.entity.Appeal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppealRepository extends JpaRepository<Appeal, UUID> {

    /** Còn khiếu nại chưa xử lý trong vòng thi? — dùng để chặn thăng hạng. */
    boolean existsByRoundIdAndStatus(UUID roundId, AppealStatus status);

    /** Chặn một đội nộp trùng khiếu nại đang chờ xử lý cho cùng một vòng. */
    boolean existsByRoundIdAndTeamIdAndStatus(UUID roundId, UUID teamId, AppealStatus status);

    @EntityGraph(attributePaths = {"event", "round", "team", "submittedBy", "resolvedBy"})
    Optional<Appeal> findWithRelationsById(UUID id);

    /** Tìm kiếm cho màn hình điều phối viên: lọc theo sự kiện / vòng / trạng thái. */
    @EntityGraph(attributePaths = {"event", "round", "team", "submittedBy", "resolvedBy"})
    @Query("""
            select a from Appeal a
            where (:eventId is null or a.event.id = :eventId)
              and (:roundId is null or a.round.id = :roundId)
              and (:status is null or a.status = :status)
            order by a.createdAt desc
            """)
    Page<Appeal> search(@Param("eventId") UUID eventId, @Param("roundId") UUID roundId,
                        @Param("status") AppealStatus status, Pageable pageable);

    /** Khiếu nại của một đội (để thí sinh theo dõi trạng thái đơn của mình). */
    @EntityGraph(attributePaths = {"event", "round", "team", "submittedBy", "resolvedBy"})
    List<Appeal> findByTeamIdOrderByCreatedAtDesc(UUID teamId);
}
