package vn.edu.fpt.seal.modules.team.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.seal.common.enums.TeamMemberRole;
import vn.edu.fpt.seal.modules.team.entity.TeamMember;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {

    @EntityGraph(attributePaths = {"user"})
    List<TeamMember> findByTeamIdOrderByRoleAscJoinedAtAsc(UUID teamId);

    boolean existsByTeamIdAndUserId(UUID teamId, UUID userId);

    long countByTeamId(UUID teamId);

    boolean existsByTeamIdAndRole(UUID teamId, TeamMemberRole role);

    Optional<TeamMember> findByTeamIdAndUserId(UUID teamId, UUID userId);

    @EntityGraph(attributePaths = {"team", "team.track", "team.track.event", "user"})
    List<TeamMember> findByUserIdOrderByJoinedAtDesc(UUID userId);
}
