package controller.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import controller.model.repository.*;
import jakarta.servlet.http.HttpSession;

@Controller
public class AppointmentController {
    private static final List<String> STATUSES = List.of("UPCOMING", "CONFIRMED", "COMPLETED", "CANCELLED");
    private final AppointmentRepository appointments;
    private final UserRepository users;
    private final DonationRepository donations;
    private final HealthAssessmentRepository assessments;

    public AppointmentController(AppointmentRepository a, UserRepository u, DonationRepository d, HealthAssessmentRepository h) {
        appointments = a; users = u; donations = d; assessments = h;
    }

    @GetMapping("/donation")
    public String page(HttpSession s, Model m) {
        if (!logged(s)) return "redirect:/login";
        boolean isAdmin = admin(s);
        m.addAttribute("admin", isAdmin); m.addAttribute("appointment", new Appointment());
        m.addAttribute("appointments", isAdmin ? appointments.findAllByOrderByAppointmentDateAscAppointmentTimeAsc() : appointments.findByUserIdOrderByAppointmentDateAscAppointmentTimeAsc(uid(s)));
        if (isAdmin) { m.addAttribute("donors", users.findByRoleIgnoreCaseOrderByLastNameAscFirstNameAsc("DONOR")); java.util.Map<Long,String> names=new java.util.HashMap<>();users.findByRoleIgnoreCaseOrderByLastNameAscFirstNameAsc("DONOR").forEach(u->names.put(u.getId(),u.getDonorId()+" · "+u.getFirstName()+" "+u.getLastName()));m.addAttribute("donorNames",names); }
        add(s, m); return "donation";
    }

    @PostMapping("/donation")
    public String create(Appointment a, HttpSession s, RedirectAttributes f) {
        if (!logged(s) || admin(s)) return "redirect:/dashboard";
        if (!validDate(a.getAppointmentDate(), a.getAppointmentTime())) { f.addFlashAttribute("error", "Choose a future date and a valid time."); return "redirect:/donation"; }
        a.setId(null); a.setUserId(uid(s)); a.setLocation(clean(a.getLocation()).isEmpty() ? "LifeLink Donation Center" : clean(a.getLocation())); a.setStatus("UPCOMING");
        appointments.save(a); f.addFlashAttribute("success", "Appointment scheduled."); return "redirect:/donation";
    }

    @PostMapping("/donation/{id}/reschedule")
    public String reschedule(@PathVariable Long id, @RequestParam LocalDate appointmentDate, @RequestParam LocalTime appointmentTime,
                             @RequestParam(required=false) String location, HttpSession s, RedirectAttributes f) {
        var found = appointments.findById(id);
        if (found.isEmpty() || (!admin(s) && !found.get().getUserId().equals(uid(s)))) return "redirect:/donation";
        Appointment a = found.get();
        if (!validDate(appointmentDate, appointmentTime) || "COMPLETED".equalsIgnoreCase(a.getStatus()) || "CANCELLED".equalsIgnoreCase(a.getStatus())) {
            f.addFlashAttribute("error", "This appointment cannot be rescheduled to that date."); return "redirect:/donation";
        }
        a.setAppointmentDate(appointmentDate); a.setAppointmentTime(appointmentTime);
        if (admin(s) && !clean(location).isEmpty()) a.setLocation(clean(location));
        a.setStatus("UPCOMING"); appointments.save(a); f.addFlashAttribute("success", "Appointment rescheduled."); return "redirect:/donation";
    }

    @PostMapping("/donation/{id}/cancel")
    public String cancel(@PathVariable Long id, HttpSession s, RedirectAttributes f) {
        var found = appointments.findById(id);
        if (found.isPresent() && (admin(s) || found.get().getUserId().equals(uid(s))) && !"COMPLETED".equalsIgnoreCase(found.get().getStatus())) {
            found.get().setStatus("CANCELLED"); appointments.save(found.get()); f.addFlashAttribute("success", "Appointment cancelled.");
        }
        return "redirect:/donation";
    }

    @Transactional
    @PostMapping("/donation/{id}/status")
    public String status(@PathVariable Long id, @RequestParam String status, @RequestParam(required=false) Integer collectedUnits, @RequestParam(required=false) Long userId, @RequestParam(required=false) LocalDate appointmentDate, @RequestParam(required=false) LocalTime appointmentTime, @RequestParam(required=false) String location, HttpSession s, RedirectAttributes f) {
        if (!admin(s)) return "redirect:/dashboard";
        var found = appointments.findByIdForUpdate(id);
        if (found.isEmpty()) { f.addFlashAttribute("error", "Appointment not found."); return "redirect:/donation"; }
        Appointment a = found.get(); String next = status == null ? "" : status.trim().toUpperCase();
        if (!STATUSES.contains(next)) { f.addFlashAttribute("error", "Choose a valid appointment status."); return "redirect:/donation"; }
        if ("COMPLETED".equalsIgnoreCase(a.getStatus()) || "CANCELLED".equalsIgnoreCase(a.getStatus())) { f.addFlashAttribute("error", "Completed or cancelled appointments cannot be changed."); return "redirect:/donation"; }
        if (userId != null) { User selected=users.findById(userId).filter(u->"DONOR".equalsIgnoreCase(u.getRole())).orElse(null); if(selected==null){f.addFlashAttribute("error","Select a valid donor.");return "redirect:/donation";} a.setUserId(userId); }
        if (appointmentDate != null || appointmentTime != null) {
            boolean unchanged=appointmentDate!=null&&appointmentDate.equals(a.getAppointmentDate())&&appointmentTime!=null&&appointmentTime.equals(a.getAppointmentTime());
            boolean completionDate="COMPLETED".equals(next)&&unchanged&&appointmentDate!=null&&!appointmentDate.isAfter(LocalDate.now());
            if(!completionDate&&!validDate(appointmentDate,appointmentTime)){f.addFlashAttribute("error","Scheduled appointments must use a future date and valid time.");return "redirect:/donation";}
            a.setAppointmentDate(appointmentDate);a.setAppointmentTime(appointmentTime);
        }
        if(location!=null&&!location.isBlank())a.setLocation(location.trim());
        if ("COMPLETED".equals(next)) {
            if (donations.existsByAppointmentId(id)) { a.setStatus("COMPLETED"); appointments.save(a); f.addFlashAttribute("success", "This completed donation is already recorded."); return "redirect:/donation"; }
            if (collectedUnits == null || collectedUnits < 1 || collectedUnits > 10 || a.getAppointmentDate() == null || a.getAppointmentDate().isAfter(LocalDate.now())) {
                f.addFlashAttribute("error", "Enter at least one collected bag and complete the appointment on or after its scheduled date."); return "redirect:/donation";
            }
            User donor = users.findById(a.getUserId()).filter(u -> "DONOR".equalsIgnoreCase(u.getRole())).orElse(null);
            if (donor == null || donor.getBloodType() == null || !donor.getBloodType().matches("^(A|B|AB|O)[+-]$")) { f.addFlashAttribute("error", "The appointment donor or blood type is invalid."); return "redirect:/donation"; }
            Donation donation = new Donation(); donation.setUserId(donor.getId()); donation.setAppointmentId(id); donation.setDonationDate(a.getAppointmentDate());
            donation.setBloodType(donor.getBloodType()); donation.setUnits(collectedUnits); donation.setLocation(a.getLocation());
            donation.setStatus("COMPLETED"); donation.setNextEligibleDate(a.getAppointmentDate().plusDays(90)); donation.setInventoryAccepted(false);
            donations.save(donation);
            assessments.findFirstByUserIdOrderBySubmittedAtDesc(donor.getId()).ifPresent(assessment -> {
                if (assessment.getEligibilityStatus() != null && "ELIGIBLE".equalsIgnoreCase(assessment.getEligibilityStatus())) assessment.setEligibilityStatus("IN PROCESS");
                assessments.save(assessment);
            });
            a.setCollectedUnits(collectedUnits);
        }
        a.setStatus(next); appointments.save(a); f.addFlashAttribute("success", "Appointment updated." + ("COMPLETED".equals(next) ? " Donation recorded in history; inventory awaits acceptance." : ""));
        return "redirect:/donation";
    }

    private boolean validDate(LocalDate date, LocalTime time) { return date != null && !date.isBefore(LocalDate.now()) && time != null; }
    private String clean(String value) { return value == null ? "" : value.trim(); }
    private boolean logged(HttpSession s) { return s.getAttribute("userId") != null; }
    private boolean admin(HttpSession s) { return "ADMIN".equalsIgnoreCase(String.valueOf(s.getAttribute("role"))); }
    private Long uid(HttpSession s) { return ((Number)s.getAttribute("userId")).longValue(); }
    private void add(HttpSession s, Model m) { m.addAttribute("firstName", s.getAttribute("firstName")); }
}
