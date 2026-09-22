package controller.model;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import controller.model.repository.Appointment;
import controller.model.repository.AppointmentRepository;
import controller.model.repository.UserRepository;
import controller.model.repository.HealthAssessmentRepository;
import controller.model.repository.DonationRepository;
import controller.model.repository.BloodRequestRepository;
import controller.model.repository.BloodInventoryRepository;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final HealthAssessmentRepository healthAssessmentRepository;
    private final DonationRepository donationRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final BloodInventoryRepository bloodInventoryRepository;

    public HomeController(
            UserRepository userRepository,
            AppointmentRepository appointmentRepository,
            HealthAssessmentRepository healthAssessmentRepository,
            DonationRepository donationRepository,
            BloodRequestRepository bloodRequestRepository,
            BloodInventoryRepository bloodInventoryRepository) {

        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.healthAssessmentRepository = healthAssessmentRepository;
        this.donationRepository = donationRepository;
        this.bloodRequestRepository = bloodRequestRepository;
        this.bloodInventoryRepository = bloodInventoryRepository;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/learn-more")
    public String learnMore() {
        return "learn-more";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        Object sessionUserId = session.getAttribute("userId");

        if (sessionUserId == null) {
            return "redirect:/login";
        }

        Long userId = ((Number) sessionUserId).longValue();

        String firstName = (String) session.getAttribute("firstName");
        String lastName = (String) session.getAttribute("lastName");
        String role = (String) session.getAttribute("role");
        String bloodType = (String) session.getAttribute("bloodType");

        boolean admin = role != null
                && role.equalsIgnoreCase("ADMIN");


        // =========================
        // USER STATISTICS
        // =========================

        long totalUsers = userRepository.count();

        long totalDonors = userRepository.findAll()
                .stream()
                .filter(user ->
                        user.getRole() != null
                        && user.getRole().equalsIgnoreCase("DONOR"))
                .count();

        long totalAdmins = userRepository.findAll()
                .stream()
                .filter(user ->
                        user.getRole() != null
                        && user.getRole().equalsIgnoreCase("ADMIN"))
                .count();


        // =========================
        // APPOINTMENT STATISTICS
        // =========================

        long totalAppointments = appointmentRepository.count();

        long upcomingAppointments =
                appointmentRepository.countByStatus("UPCOMING");


        // =========================
        // DONOR APPOINTMENTS
        // =========================

        List<Appointment> userAppointments =
                appointmentRepository
                        .findByUserIdOrderByAppointmentDateAscAppointmentTimeAsc(
                                userId
                        );

        Appointment upcomingAppointment = null;

        for (Appointment appointment : userAppointments) {

            if (appointment.getStatus() != null
                    && appointment.getStatus()
                    .equalsIgnoreCase("UPCOMING")) {

                upcomingAppointment = appointment;
                break;
            }
        }


        // =========================
        // SEND DATA TO DASHBOARD
        // =========================

        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("role", role);
        model.addAttribute("bloodType", bloodType);
        model.addAttribute("admin", admin);

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalDonors", totalDonors);
        model.addAttribute("totalAdmins", totalAdmins);

        model.addAttribute(
                "totalAppointments",
                totalAppointments
        );

        model.addAttribute(
                "upcomingAppointments",
                upcomingAppointments
        );

        model.addAttribute(
                "upcomingAppointment",
                upcomingAppointment
        );

        model.addAttribute("assessment", healthAssessmentRepository.findFirstByUserIdOrderBySubmittedAtDesc(userId).orElse(null));
        model.addAttribute("donationHistory", donationRepository.findByUserIdOrderByDonationDateDesc(userId));
        model.addAttribute("totalDonations", donationRepository.findByUserIdOrderByDonationDateDesc(userId).stream().filter(d -> "COMPLETED".equalsIgnoreCase(d.getStatus())).count());
        model.addAttribute("bloodRequests", bloodRequestRepository.findAllByOrderByCreatedAtDesc());
        model.addAttribute("allAppointments", appointmentRepository.findAllByOrderByAppointmentDateAscAppointmentTimeAsc());
        model.addAttribute("inventory", bloodInventoryRepository.findAll());
        model.addAttribute("totalCompletedDonations", donationRepository.countByStatus("COMPLETED"));
        model.addAttribute("activeRequests", bloodRequestRepository.countByStatusNot("COMPLETED"));

        return "dashboard";
    }
}
