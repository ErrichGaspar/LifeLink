package controller.model.repository;
import java.util.List; import org.springframework.data.jpa.repository.JpaRepository;
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> { List<BloodRequest> findAllByOrderByCreatedAtDesc(); long countByStatusNot(String status); }
