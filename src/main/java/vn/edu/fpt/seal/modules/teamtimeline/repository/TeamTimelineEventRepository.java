package vn.edu.fpt.seal.modules.teamtimeline.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.seal.modules.teamtimeline.entity.TeamTimelineEvent;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeamTimelineEventRepository extends JpaRepository<TeamTimelineEvent, UUID> {

    /** Toàn bộ mốc của một đội, mới nhất trước. */
    @EntityGraph(attributePaths = {"team", "round", "event"})
    List<TeamTimelineEvent> findByTeamIdOrderByOccurredAtDesc(UUID teamId);

    /** Mốc của mọi đội trong một sự kiện — dùng cho màn hình của điều phối viên (EC). */
    @EntityGraph(attributePaths = {"team", "round", "event"})
    Page<TeamTimelineEvent> findByEventIdOrderByOccurredAtDesc(UUID eventId, Pageable pageable);
}
