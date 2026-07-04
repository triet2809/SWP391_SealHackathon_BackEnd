package vn.edu.fpt.seal.modules.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.seal.modules.notification.entity.Notification;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findTop100ByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Notification> findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(UUID userId);

    long countByUserIdAndReadAtIsNull(UUID userId);

    /** Unread counts grouped by category for the calling user (powers the red dots). */
    @Query("select n.category as category, count(n) as cnt from Notification n " +
            "where n.user.id = :userId and n.readAt is null group by n.category")
    List<CategoryCount> countUnreadByCategory(@Param("userId") UUID userId);

    interface CategoryCount {
        String getCategory();
        long getCnt();
    }
}
