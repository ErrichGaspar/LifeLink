package controller.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    List<Appointment> findByUserIdOrderByAppointmentDateAscAppointmentTimeAsc(
            Long userId
    );

    long countByStatus(String status);

    List<Appointment> findAllByOrderByAppointmentDateAscAppointmentTimeAsc();
}
