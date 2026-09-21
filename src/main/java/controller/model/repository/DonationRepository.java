package controller.model.repository;
import java.util.List; import org.springframework.data.jpa.repository.JpaRepository;
public interface DonationRepository extends JpaRepository<Donation, Long> { List<Donation> findByUserIdOrderByDonationDateDesc(Long userId); long countByStatus(String status); List<Donation> findAllByOrderByDonationDateDesc(); }
