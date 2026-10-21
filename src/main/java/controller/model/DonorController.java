package controller.model;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import controller.model.repository.User;
import controller.model.repository.UserRepository;
import controller.model.repository.DonationRepository;
import controller.model.repository.AppointmentRepository;
import controller.model.repository.HealthAssessmentRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import jakarta.servlet.http.HttpSession;

@Controller
public class DonorController {

    private final UserRepository userRepository;
    private final DonationRepository donations;
    private final AppointmentRepository appointments;
    private final HealthAssessmentRepository assessments;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public DonorController(UserRepository userRepository, DonationRepository donations, AppointmentRepository appointments, HealthAssessmentRepository assessments) {
        this.userRepository = userRepository;
        this.donations = donations; this.appointments = appointments; this.assessments = assessments;
    }

    @GetMapping("/donors")
    public String donors(@RequestParam(required = false, defaultValue = "") String search,
                         HttpSession session,
                         Model model) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }

        boolean admin = isAdmin(session);
        model.addAttribute("admin", admin);
        addSessionProfile(model, session);

        if (admin) {
            String trimmedSearch = search.trim();
            List<User> donors = trimmedSearch.isEmpty()
                    ? userRepository.findByRoleIgnoreCaseOrderByLastNameAscFirstNameAsc("DONOR")
                    : userRepository.searchByRole("DONOR", trimmedSearch);
            model.addAttribute("donors", donors);
            model.addAttribute("search", trimmedSearch);
            java.util.Map<Long,String> eligibility = new java.util.HashMap<>();
            java.util.Map<Long,String> latestDonation = new java.util.HashMap<>();
            for (User item : donors) {
                String current = assessments.findFirstByUserIdOrderBySubmittedAtDesc(item.getId()).map(a -> a.getEligibilityStatus()).orElse("Not assessed");
                java.time.LocalDate last = donations.findByUserIdAndStatusIgnoreCaseOrderByDonationDateDesc(item.getId(), "COMPLETED").stream().map(d -> d.getDonationDate()).filter(java.util.Objects::nonNull).findFirst().orElse(null);
                if (last != null && last.plusDays(90).isAfter(java.time.LocalDate.now())) current = "IN PROCESS";
                else current = assessments.findFirstByUserIdOrderBySubmittedAtDesc(item.getId()).map(a -> a.getReadinessScore()==null?a.getEligibilityStatus():a.getReadinessScore()>=70?"ELIGIBLE":a.getReadinessScore()>=45?"IN PROCESS":"NOT ELIGIBLE").orElse("Not assessed");
                eligibility.put(item.getId(), current); if (last != null) latestDonation.put(item.getId(), last.toString());
            }
            model.addAttribute("eligibility", eligibility); model.addAttribute("latestDonation", latestDonation);
            return "donors";
        }

        Long userId = ((Number) session.getAttribute("userId")).longValue();
        User donor = userRepository.findById(userId).orElse(null);
        model.addAttribute("donor", donor);
        return "donors";
    }

    @GetMapping("/donors/add")
    public String addDonorForm(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/dashboard";
        }
        model.addAttribute("admin", true);
        model.addAttribute("formMode", "add");
        model.addAttribute("donor", new User());
        addSessionProfile(model, session);
        return "donors";
    }

    @PostMapping("/donors/add")
    public String addDonor(User donor,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/dashboard";
        }

        String username = clean(donor.getUsername());
        if (requiredFieldsMissing(donor) || username.isEmpty() || clean(donor.getPassword()).isEmpty()) {
            return donorFormError(model, session, donor,
                    "First name, last name, username, blood type, and a temporary password are required.", "add");
        }
        donor.setUsername(username);
        if (userRepository.findByUsername(username).isPresent()) {
            return donorFormError(model, session, donor, "That username is already in use.", "add");
        }

        donor.setRole("DONOR");
        donor.setId(null);
        donor.setPassword(encoder.encode(donor.getPassword()));
        userRepository.save(donor);
        redirectAttributes.addFlashAttribute("success", "Donor added successfully.");
        return "redirect:/donors";
    }

    @GetMapping("/donors/{id}/edit")
    public String editDonorForm(@PathVariable Long id, HttpSession session, Model model,
                                RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/dashboard";
        }

        Optional<User> donor = findDonor(id);
        if (donor.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Donor record not found.");
            return "redirect:/donors";
        }

        model.addAttribute("admin", true);
        model.addAttribute("formMode", "edit");
        model.addAttribute("donor", donor.get());
        addSessionProfile(model, session);
        return "donors";
    }

    @PostMapping("/donors/{id}/edit")
    public String updateDonor(@PathVariable Long id,
                              User formDonor,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/dashboard";
        }

        Optional<User> existing = findDonor(id);
        if (existing.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Donor record not found.");
            return "redirect:/donors";
        }

        User donor = existing.get();
        String username = clean(formDonor.getUsername());
        if (requiredFieldsMissing(formDonor) || username.isEmpty()) {
            return donorFormError(model, session, formDonor,
                    "First name, last name, username, and blood type are required.", "edit");
        }
        formDonor.setUsername(username);
        Optional<User> usernameOwner = userRepository.findByUsername(username);
        if (usernameOwner.isPresent() && !usernameOwner.get().getId().equals(id)) {
            return donorFormError(model, session, formDonor, "That username is already in use.", "edit");
        }

        donor.setFirstName(formDonor.getFirstName());
        donor.setLastName(formDonor.getLastName());
        donor.setUsername(username);
        donor.setPhoneNumber(formDonor.getPhoneNumber());
        donor.setBloodType(formDonor.getBloodType());
        // Password is intentionally retained during profile edits.
        userRepository.save(donor);

        redirectAttributes.addFlashAttribute("success", "Donor information updated.");
        return "redirect:/donors";
    }

    @PostMapping("/donors/{id}/delete")
    public String deleteDonor(@PathVariable Long id, HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/dashboard";
        }

        Optional<User> donor = findDonor(id);
        if (donor.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Donor record not found.");
        } else if (donations.existsByUserId(id) || appointments.existsByUserId(id) || assessments.findFirstByUserIdOrderBySubmittedAtDesc(id).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "This donor has related appointment, assessment, or donation history and cannot be deleted.");
        } else {
            userRepository.delete(donor.get()); redirectAttributes.addFlashAttribute("success", "Donor record deleted.");
        }
        return "redirect:/donors";
    }

    private Optional<User> findDonor(Long id) {
        return userRepository.findById(id)
                .filter(user -> user.getRole() != null && user.getRole().equalsIgnoreCase("DONOR"));
    }

    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("userId") != null;
    }

    private boolean isAdmin(HttpSession session) {
        Object role = session.getAttribute("role");
        return role != null && "ADMIN".equalsIgnoreCase(role.toString());
    }

    private void addSessionProfile(Model model, HttpSession session) {
        model.addAttribute("firstName", session.getAttribute("firstName"));
        model.addAttribute("lastName", session.getAttribute("lastName"));
        boolean admin = isAdmin(session);
        model.addAttribute("admin", admin);
        model.addAttribute("activePage", admin ? "donors" : "profile");
    }

    private String donorFormError(Model model, HttpSession session, User donor,
                                  String error, String formMode) {
        model.addAttribute("admin", true);
        model.addAttribute("formMode", formMode);
        model.addAttribute("donor", donor);
        model.addAttribute("error", error);
        addSessionProfile(model, session);
        return "donors";
    }

    private boolean requiredFieldsMissing(User donor) {
        return clean(donor.getFirstName()).isEmpty()
                || clean(donor.getLastName()).isEmpty()
                || clean(donor.getBloodType()).isEmpty();
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
