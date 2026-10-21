package controller.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import controller.model.repository.HealthAssessment;
import controller.model.repository.HealthAssessmentRepository;
import controller.model.repository.User;
import controller.model.repository.UserRepository;
import controller.model.repository.DonationRepository;
import jakarta.servlet.http.HttpSession;

@Controller
public class HealthController {
    private final HealthAssessmentRepository assessments;
    private final UserRepository users;
    private final DonationRepository donations;
    public HealthController(HealthAssessmentRepository assessments, UserRepository users, DonationRepository donations) { this.assessments = assessments; this.users = users; this.donations = donations; }

    @GetMapping("/assessment") public String assessment(HttpSession session, Model model) {
        if (!loggedIn(session)) return "redirect:/login";
        if (admin(session)) return review(session, model);
        Long id = userId(session); model.addAttribute("assessment", assessments.findFirstByUserIdOrderBySubmittedAtDesc(id).orElse(new HealthAssessment()));
        addProfile(session, model); return "assessment";
    }
    @PostMapping("/assessment") public String submit(HealthAssessment assessment, HttpSession session, RedirectAttributes flash) {
        if (!loggedIn(session) || admin(session)) return "redirect:/dashboard";
        if (assessment.getBirthDate() == null || assessment.getBirthDate().isAfter(LocalDate.now()) || assessment.getWeightKg() == null || assessment.getWeightKg() <= 0 || unanswered(assessment)) {
            flash.addFlashAttribute("error", "Please complete every health question with valid information."); return "redirect:/assessment";
        }
        assessment.setUserId(userId(session)); assessment.setSubmittedAt(LocalDateTime.now());
        int score = 100;
        boolean ageOk = Period.between(assessment.getBirthDate(), LocalDate.now()).getYears() >= 18;
        if (!ageOk || assessment.getWeightKg() < 50) score -= 45;
        if (yes(assessment.getHasFever())) score -= 35;
        if (yes(assessment.getRecentSurgery()) || yes(assessment.getHighRiskExposure()) || yes(assessment.getPregnantOrBreastfeeding())) score -= 35;
        if (yes(assessment.getTakingMedication())) score -= 15;
        if (!yes(assessment.getFeelingWell())) score -= 25;
        assessment.setReadinessScore(Math.max(0, score));
        LocalDate nextEligible = donations.findByUserIdAndStatusIgnoreCaseOrderByDonationDateDesc(userId(session), "COMPLETED").stream().map(d -> d.getNextEligibleDate()!=null?d.getNextEligibleDate():(d.getDonationDate()==null?null:d.getDonationDate().plusDays(90))).filter(java.util.Objects::nonNull).findFirst().orElse(null);
        assessment.setEligibilityStatus(nextEligible != null && nextEligible.isAfter(LocalDate.now()) ? "IN PROCESS" : score >= 70 ? "ELIGIBLE" : score >= 45 ? "IN PROCESS" : "NOT ELIGIBLE");
        assessments.save(assessment); flash.addFlashAttribute("success", "Assessment saved. Your current status is " + assessment.getEligibilityStatus() + "."); return "redirect:/assessment";
    }
    private String review(HttpSession s, Model m) { m.addAttribute("admin", true); m.addAttribute("assessments", assessments.findAllByOrderBySubmittedAtDesc()); addProfile(s,m); return "assessment"; }
    private boolean unanswered(HealthAssessment a) { return blank(a.getFeelingWell())||blank(a.getHasFever())||blank(a.getTakingMedication())||blank(a.getRecentSurgery())||blank(a.getPregnantOrBreastfeeding())||blank(a.getHighRiskExposure()); }
    private boolean yes(String v){return "YES".equalsIgnoreCase(v);} private boolean blank(String v){return v==null||v.isBlank();}
    private boolean loggedIn(HttpSession s){return s.getAttribute("userId")!=null;} private boolean admin(HttpSession s){return "ADMIN".equalsIgnoreCase(String.valueOf(s.getAttribute("role")));} private Long userId(HttpSession s){return ((Number)s.getAttribute("userId")).longValue();}
    private void addProfile(HttpSession s, Model m){m.addAttribute("firstName",s.getAttribute("firstName"));m.addAttribute("admin",admin(s));}
}
