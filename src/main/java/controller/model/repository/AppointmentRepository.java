package controller.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select a from Appointment a where a.id = :id")
    java.util.Optional<Appointment> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);

    List<Appointment> findByUserIdOrderByAppointmentDateAscAppointmentTimeAsc(
            Long userId
    );

    long countByStatus(String status);

    List<Appointment> findAllByOrderByAppointmentDateAscAppointmentTimeAsc();
    long countByStatusIgnoreCaseAndAppointmentDateGreaterThanEqual(String status, java.time.LocalDate date);
    boolean existsByUserId(Long userId);
}
