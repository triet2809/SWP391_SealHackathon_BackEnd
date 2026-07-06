package vn.edu.fpt.seal.modules.rules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.seal.modules.rules.entity.RuleAcceptance;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RuleAcceptanceRepository extends JpaRepository<RuleAcceptance, UUID> {

    boolean existsByUserIdAndEventId(UUID userId, UUID eventId);

    Optional<RuleAcceptance> findByUserIdAndEventId(UUID userId, UUID eventId);
}
