package vn.edu.fpt.seal.modules.rules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.seal.common.enums.RuleVisibility;
import vn.edu.fpt.seal.modules.rules.entity.EventRule;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRuleRepository extends JpaRepository<EventRule, UUID> {

    /** Toàn bộ rule của sự kiện (mọi mức hiển thị) — cho điều phối viên. */
    List<EventRule> findByEventIdOrderByDisplayOrderAscCreatedAtAsc(UUID eventId);

    /** Chỉ các rule ở một mức hiển thị — dùng lọc PUBLIC cho thí sinh. */
    List<EventRule> findByEventIdAndVisibilityOrderByDisplayOrderAscCreatedAtAsc(UUID eventId, RuleVisibility visibility);

    /** Sự kiện có rule PUBLIC nào không? — quyết định có bắt buộc tick chấp nhận hay không. */
    boolean existsByEventIdAndVisibility(UUID eventId, RuleVisibility visibility);
}
