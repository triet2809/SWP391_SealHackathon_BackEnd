package vn.edu.fpt.seal.modules.ranking.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.seal.modules.ranking.entity.TieBreakDecision;

import java.util.List;
import java.util.UUID;

@Repository
public interface TieBreakDecisionRepository extends JpaRepository<TieBreakDecision, UUID> {

    /** Toàn bộ quyết định thủ công của một vòng — dùng khi tính lại xếp hạng. */
    @EntityGraph(attributePaths = {"team", "decidedBy"})
    List<TieBreakDecision> findByRoundId(UUID roundId);

    boolean existsByRoundIdAndTeamId(UUID roundId, UUID teamId);
}
