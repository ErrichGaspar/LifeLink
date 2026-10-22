package controller.model.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    boolean existsByEventKey(String eventKey);
    List<Notification> findByRecipientRoleAndRecipientUserIdOrderByCreatedAtDesc(String role, Long userId);
    List<Notification> findByRecipientRoleAndRecipientUserIdIsNullOrderByCreatedAtDesc(String role);
}
