package vn.edu.fpt.seal.modules.mentor.repository;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.seal.common.enums.TeamStatus;
import vn.edu.fpt.seal.modules.mentor.entity.TrackMentor;

import java.util.*;

@Repository
public interface TrackMentorRepository extends JpaRepository<TrackMentor, UUID> {
    @EntityGraph(attributePaths = {"event", "track", "user"}) Page<TrackMentor> findByTrackId(UUID trackId, Pageable pageable);
    @EntityGraph(attributePaths = {"event", "track", "user"}) Page<TrackMentor> findByUserId(UUID userId, Pageable pageable);
    boolean existsByTrackIdAndUserId(UUID trackId, UUID userId);
    boolean existsByEventIdAndUserId(UUID eventId, UUID userId);
    Optional<TrackMentor> findByTrackIdAndUserId(UUID trackId, UUID userId);

    @Query(value = """
            select tm.id as trackMentorId, tm.user_id as mentorId, t.id as trackId, t.name as trackName,
                   r.id as roundId, r.name as roundName, team.id as teamId, team.name as teamName, team.status as teamStatus
            from track_mentors tm
            join tracks t on t.id = tm.track_id
            left join rounds r on r.track_id = t.id
            left join teams team on team.track_id = t.id
            where tm.user_id = :mentorId
            order by t.name asc, team.name asc, r.sequence_number asc
            """, nativeQuery = true)
    List<MentorTeamRow> findTeamRowsForMentor(@Param("mentorId") UUID mentorId);

    interface MentorTeamRow {
        UUID getTrackMentorId(); UUID getMentorId(); UUID getTrackId(); String getTrackName(); UUID getRoundId(); String getRoundName(); UUID getTeamId(); String getTeamName(); TeamStatus getTeamStatus();
    }
}
