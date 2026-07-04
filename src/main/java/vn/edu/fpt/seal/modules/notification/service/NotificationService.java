package vn.edu.fpt.seal.modules.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.seal.common.exception.ApiException;
import vn.edu.fpt.seal.modules.notification.dto.NotificationResponse;
import vn.edu.fpt.seal.modules.notification.entity.Notification;
import vn.edu.fpt.seal.modules.notification.repository.NotificationRepository;
import vn.edu.fpt.seal.modules.user.entity.User;
import vn.edu.fpt.seal.modules.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;
    private final UserRepository userRepository;

    // --- read side (called by controller) ---

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(UUID userId, boolean unreadOnly) {
        List<Notification> list = unreadOnly
                ? repo.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(userId)
                : repo.findTop100ByUserIdOrderByCreatedAtDesc(userId);
        return list.stream().map(this::toResponse).toList();
    }

    /** Unread total + per-category counts, used to render red dots on the sidebar. */
    @Transactional(readOnly = true)
    public Map<String, Object> unreadSummary(UUID userId) {
        Map<String, Long> byCategory = repo.countUnreadByCategory(userId).stream()
                .collect(Collectors.toMap(NotificationRepository.CategoryCount::getCategory,
                        NotificationRepository.CategoryCount::getCnt));
        long total = byCategory.values().stream().mapToLong(Long::longValue).sum();
        Map<String, Object> out = new HashMap<>();
        out.put("total", total);
        out.put("byCategory", byCategory);
        return out;
    }

    @Transactional
    public void markRead(UUID userId, UUID id) {
        Notification n = repo.findById(id).orElseThrow(() -> ApiException.notFound("Notification not found"));
        if (!n.getUser().getId().equals(userId)) throw ApiException.forbidden("Not your notification");
        if (n.getReadAt() == null) { n.setReadAt(LocalDateTime.now()); }
    }

    @Transactional
    public void markAllRead(UUID userId, String category) {
        List<Notification> unread = repo.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(userId);
        LocalDateTime now = LocalDateTime.now();
        for (Notification n : unread) {
            if (category == null || category.isBlank() || category.equalsIgnoreCase(n.getCategory())) {
                n.setReadAt(now);
            }
        }
    }

    // --- write side (called by other services to emit notifications) ---

    /** Fire-and-forget notification emit; never breaks the caller's main transaction. */
    @Transactional
    public void emit(UUID userId, String type, String category, String title, String body, String refType, UUID refId) {
        if (userId == null) return;
        try {
            User u = userRepository.getReferenceById(userId);
            repo.save(Notification.builder()
                    .user(u).type(type).category(category)
                    .title(title).body(body)
                    .refType(refType).refId(refId)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to emit notification (type={}, user={}): {}", type, userId, e.getMessage());
        }
    }

    public void emitAll(Collection<UUID> userIds, String type, String category, String title, String body, String refType, UUID refId) {
        if (userIds == null) return;
        for (UUID uid : new LinkedHashSet<>(userIds)) emit(uid, type, category, title, body, refType, refId);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .body(n.getBody())
                .category(n.getCategory())
                .refType(n.getRefType())
                .refId(n.getRefId())
                .readAt(n.getReadAt())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
