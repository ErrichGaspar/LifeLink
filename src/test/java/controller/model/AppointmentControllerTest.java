package controller.model;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import controller.model.repository.Appointment;
import controller.model.repository.AppointmentRepository;
import controller.model.repository.Donation;
import controller.model.repository.DonationRepository;
import controller.model.repository.HealthAssessment;
import controller.model.repository.HealthAssessmentRepository;
import controller.model.repository.UserRepository;
import jakarta.servlet.http.HttpSession;

class AppointmentControllerTest {
    @Test
    void donorWithoutAssessmentCannotSchedule() {
        List<Appointment> saved = new ArrayList<>();
        AppointmentController controller = controller(null, List.of(), saved);
        RedirectAttributesModelMap flash = new RedirectAttributesModelMap();

        String result = controller.create(appointment(), donorSession(), flash);

        assertEquals("redirect:/donation", result);
        assertTrue(saved.isEmpty());
        assertNotNull(flash.getFlashAttributes().get("error"));
    }

    @Test
    void eligibleDonorCanSchedule() {
        HealthAssessment assessment = new HealthAssessment();
        assessment.setReadinessScore(85);
        assessment.setEligibilityStatus("ELIGIBLE");
        assessment.setSubmittedAt(LocalDateTime.now());
        List<Appointment> saved = new ArrayList<>();
        AppointmentController controller = controller(assessment, List.of(), saved);

        String result = controller.create(appointment(), donorSession(), new RedirectAttributesModelMap());

        assertEquals("redirect:/donation", result);
        assertEquals(1, saved.size());
        assertEquals(41L, saved.get(0).getUserId());
        assertEquals("UPCOMING", saved.get(0).getStatus());
    }

    @Test
    void ineligibleDonorCannotScheduleEvenWhenRetakeCooldownIsOver() {
        HealthAssessment assessment = new HealthAssessment();
        assessment.setEligibilityStatus("INELIGIBLE");
        assessment.setSubmittedAt(LocalDateTime.now().minusHours(2));
        List<Appointment> saved = new ArrayList<>();
        AppointmentController controller = controller(assessment, List.of(), saved);

        String result = controller.create(appointment(), donorSession(), new RedirectAttributesModelMap());

        assertEquals("redirect:/donation", result);
        assertTrue(saved.isEmpty());
    }

    @Test
    void oldAssessmentCannotBeReusedAfterCompletedDonation() {
        HealthAssessment assessment = new HealthAssessment();
        assessment.setReadinessScore(90);
        assessment.setEligibilityStatus("ELIGIBLE");
        assessment.setSubmittedAt(LocalDateTime.now().minusDays(120));
        Donation donation = new Donation();
        donation.setDonationDate(LocalDate.now().minusDays(100));
        donation.setNextEligibleDate(LocalDate.now().minusDays(10));
        List<Appointment> saved = new ArrayList<>();
        AppointmentController controller = controller(assessment, List.of(donation), saved);

        String result = controller.create(appointment(), donorSession(), new RedirectAttributesModelMap());

        assertEquals("redirect:/donation", result);
        assertTrue(saved.isEmpty());
    }

    private AppointmentController controller(HealthAssessment assessment, List<Donation> donationList,
                                               List<Appointment> savedAppointments) {
        AppointmentRepository appointments = proxy(AppointmentRepository.class, (proxy, method, args) -> {
            if (method.getName().equals("save")) { savedAppointments.add((Appointment) args[0]); return args[0]; }
            return null;
        });
        HealthAssessmentRepository assessments = proxy(HealthAssessmentRepository.class, (proxy, method, args) ->
                method.getName().equals("findFirstByUserIdOrderBySubmittedAtDesc") ? Optional.ofNullable(assessment) : null);
        DonationRepository donations = proxy(DonationRepository.class, (proxy, method, args) ->
                method.getName().equals("findByUserIdAndStatusIgnoreCaseOrderByDonationDateDesc") ? donationList : null);
        UserRepository users = proxy(UserRepository.class, (proxy, method, args) -> null);
        controller.model.repository.NotificationRepository notifications = proxy(controller.model.repository.NotificationRepository.class, (proxy, method, args) -> {
            if (method.getName().equals("existsByEventKey")) return false;
            if (method.getName().equals("save")) return args[0];
            return null;
        });
        return new AppointmentController(appointments, users, donations, assessments, notifications);
    }

    private Appointment appointment() {
        Appointment appointment = new Appointment();
        appointment.setAppointmentDate(LocalDate.now().plusDays(2));
        appointment.setAppointmentTime(LocalTime.of(10, 0));
        return appointment;
    }

    private HttpSession donorSession() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userId", 41L);
        attributes.put("role", "DONOR");
        return proxy(HttpSession.class, (proxy, method, args) -> {
            if (method.getName().equals("getAttribute")) return attributes.get(args[0]);
            if (method.getName().equals("setAttribute")) { attributes.put((String) args[0], args[1]); return null; }
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, java.lang.reflect.InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler);
    }
}
