package controller.model.repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface HealthAssessmentRepository extends JpaRepository<HealthAssessment, Long> {
    Optional<HealthAssessment> findFirstByUserIdOrderBySubmittedAtDesc(Long userId);
    List<HealthAssessment> findAllByOrderBySubmittedAtDesc();
}
