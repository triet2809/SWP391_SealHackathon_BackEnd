package vn.edu.fpt.seal.modules.rules.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.seal.common.enums.RuleVisibility;
import vn.edu.fpt.seal.common.exception.ApiException;
import vn.edu.fpt.seal.modules.event.entity.Event;
import vn.edu.fpt.seal.modules.event.repository.EventRepository;
import vn.edu.fpt.seal.modules.rules.dto.*;
import vn.edu.fpt.seal.modules.rules.entity.EventRule;
import vn.edu.fpt.seal.modules.rules.entity.RuleAcceptance;
import vn.edu.fpt.seal.modules.rules.repository.EventRuleRepository;
import vn.edu.fpt.seal.modules.rules.repository.RuleAcceptanceRepository;
import vn.edu.fpt.seal.modules.user.repository.UserRepository;
import vn.edu.fpt.seal.security.CurrentUser;

import java.util.List;
import java.util.UUID;

/**
 * Service quản lý thể lệ sự kiện và việc chấp nhận thể lệ.
 * NGUYÊN TẮC AN TOÀN: rule INTERNAL/DISPUTE_ONLY không bao giờ được trả về
 * cho người dùng không phải điều phối viên — lọc ngay ở tầng service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventRuleService {

    private final EventRuleRepository ruleRepository;
    private final RuleAcceptanceRepository acceptanceRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    /**
     * Danh sách rule của một sự kiện.
     * Điều phối viên: thấy tất cả. Người khác: CHỈ thấy PUBLIC.
     */
    @Transactional(readOnly = true)
    public List<EventRuleResponse> list(UUID eventId, Authentication auth) {
        if (!eventRepository.existsById(eventId)) throw ApiException.notFound("Event not found: " + eventId);
        List<EventRule> rules = isCoordinator(auth)
                ? ruleRepository.findByEventIdOrderByDisplayOrderAscCreatedAtAsc(eventId)
                : ruleRepository.findByEventIdAndVisibilityOrderByDisplayOrderAscCreatedAtAsc(eventId, RuleVisibility.PUBLIC);
        return rules.stream().map(this::toResponse).toList();
    }

    @Transactional
    public EventRuleResponse create(CreateEventRuleRequest req) {
        Event event = eventRepository.findById(req.eventId())
                .orElseThrow(() -> ApiException.notFound("Event not found: " + req.eventId()));
        EventRule rule = ruleRepository.save(EventRule.builder()
                .event(event)
                .title(req.title().trim())
                .content(req.content().trim())
                .visibility(req.visibility() == null ? RuleVisibility.PUBLIC : req.visibility())
                .displayOrder(req.displayOrder() == null ? 0 : req.displayOrder())
                .build());
        log.info("Event rule created: id={}, event={}, visibility={}", rule.getId(), event.getId(), rule.getVisibility());
        return toResponse(rule);
    }

    @Transactional
    public EventRuleResponse update(UUID id, UpdateEventRuleRequest req) {
        EventRule rule = findOrThrow(id);
        if (req.title() != null) rule.setTitle(req.title().trim());
        if (req.content() != null) rule.setContent(req.content().trim());
        if (req.visibility() != null) rule.setVisibility(req.visibility());
        if (req.displayOrder() != null) rule.setDisplayOrder(req.displayOrder());
        return toResponse(rule);
    }

    @Transactional
    public void delete(UUID id) {
        ruleRepository.delete(findOrThrow(id));
    }

    /**
     * Người dùng chấp nhận thể lệ của sự kiện (checkbox trên UI đăng ký).
     * Idempotent: đã chấp nhận rồi thì trả về bản ghi cũ.
     */
    @Transactional
    public RuleAcceptanceResponse accept(AcceptRulesRequest req, Authentication auth) {
        UUID callerId = currentUserId(auth);
        Event event = eventRepository.findById(req.eventId())
                .orElseThrow(() -> ApiException.notFound("Event not found: " + req.eventId()));
        RuleAcceptance acceptance = acceptanceRepository.findByUserIdAndEventId(callerId, event.getId())
                .orElseGet(() -> acceptanceRepository.save(RuleAcceptance.builder()
                        .user(userRepository.findById(callerId)
                                .orElseThrow(() -> ApiException.notFound("User not found: " + callerId)))
                        .event(event)
                        .build()));
        return toAcceptanceResponse(acceptance, callerId, event.getId());
    }

    /** Kiểm tra người dùng hiện tại đã chấp nhận thể lệ của sự kiện chưa (cho FE hiển thị checkbox). */
    @Transactional(readOnly = true)
    public RuleAcceptanceResponse myAcceptance(UUID eventId, Authentication auth) {
        UUID callerId = currentUserId(auth);
        return acceptanceRepository.findByUserIdAndEventId(callerId, eventId)
                .map(a -> toAcceptanceResponse(a, callerId, eventId))
                .orElse(RuleAcceptanceResponse.builder()
                        .userId(callerId).eventId(eventId).accepted(false).build());
    }

    // ==== Helpers ====

    private EventRule findOrThrow(UUID id) {
        return ruleRepository.findById(id).orElseThrow(() -> ApiException.notFound("Event rule not found: " + id));
    }

    private EventRuleResponse toResponse(EventRule r) {
        return EventRuleResponse.builder()
                .id(r.getId())
                .eventId(r.getEvent().getId())
                .title(r.getTitle())
                .content(r.getContent())
                .visibility(r.getVisibility())
                .displayOrder(r.getDisplayOrder())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    private RuleAcceptanceResponse toAcceptanceResponse(RuleAcceptance a, UUID userId, UUID eventId) {
        return RuleAcceptanceResponse.builder()
                .id(a.getId())
                .userId(userId)
                .eventId(eventId)
                .accepted(true)
                .acceptedAt(a.getAcceptedAt())
                .build();
    }

    private UUID currentUserId(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof CurrentUser c) return c.getId();
        throw ApiException.forbidden("Authentication required");
    }

    private boolean isCoordinator(Authentication auth) {
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COORDINATOR"));
    }
}
