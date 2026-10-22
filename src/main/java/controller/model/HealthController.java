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
    private static final java.time.ZoneId PHILIPPINE_TIME = java.time.ZoneId.of("Asia/Manila");
    private final HealthAssessmentRepository assessments;
    private final UserRepository users;
    private final DonationRepository donations;
    private final controller.model.repository.NotificationRepository notifications;
    public HealthController(HealthAssessmentRepository assessments, UserRepository users, DonationRepository donations, controller.model.repository.NotificationRepository notifications) { this.assessments = assessments; this.users = users; this.donations = donations; this.notifications = notifications; }

    @GetMapping("/assessment") public String assessment(HttpSession session, Model model) {
        if (!loggedIn(session)) return "redirect:/login";
        if (admin(session)) return review(session, model);
        Long id = userId(session); HealthAssessment latest = assessments.findFirstByUserIdOrderBySubmittedAtDesc(id).orElse(new HealthAssessment()); model.addAttribute("assessment", latest);
        LocalDateTime now = LocalDateTime.now(PHILIPPINE_TIME);
        LocalDateTime restriction = latest.getRetakeAllowedAt();
        var completed = donations.findByUserIdAndStatusIgnoreCaseOrderByDonationDateDesc(id, "COMPLETED").stream().findFirst().orElse(null);
        if (completed != null && completed.getCompletedAt() != null) {
            LocalDateTime donationRestriction = completed.getCompletedAt().plusHours(1);
            if (restriction == null || donationRestriction.isAfter(restriction)) restriction = donationRestriction;
        }
        model.addAttribute("retakeAllowedAt", restriction);
        model.addAttribute("cooldownActive", restriction != null && restriction.isAfter(now));
        if (latest.getId() != null && restriction != null && !restriction.isAfter(now))
            createNotification(id,"DONOR","Eligibility test available","Your eligibility test cooldown has ended. You may take the test again.","ELIGIBILITY_COOLDOWN_EXPIRED","eligibility-expired-"+latest.getId()+"-"+restriction,latest.getId());
        model.addAttribute("activePage", "assessment");
        addProfile(session, model); return "assessment";
    }
    @PostMapping("/assessment") public String submit(HealthAssessment assessment, HttpSession session, RedirectAttributes flash) {
        if (!loggedIn(session) || admin(session)) return "redirect:/dashboard";
        Long donorId = userId(session);
        HealthAssessment previous = assessments.findFirstByUserIdOrderBySubmittedAtDesc(donorId).orElse(null);
        LocalDateTime now = LocalDateTime.now(PHILIPPINE_TIME);
        LocalDateTime restriction = previous == null ? null : previous.getRetakeAllowedAt();
        var completed = donations.findByUserIdAndStatusIgnoreCaseOrderByDonationDateDesc(donorId, "COMPLETED").stream().findFirst().orElse(null);
        if (completed != null && completed.getCompletedAt() != null) {
            LocalDateTime donationRestriction = completed.getCompletedAt().plusHours(1);
            if (restriction == null || donationRestriction.isAfter(restriction)) restriction = donationRestriction;
        }
        if (restriction != null && restriction.isAfter(now)) {
            flash.addFlashAttribute("error", "You can retake the eligibility test after " + restriction + " (Philippine time)."); return "redirect:/assessment";
        }
        if (assessment.getBirthDate() == null || assessment.getBirthDate().isAfter(LocalDate.now(PHILIPPINE_TIME)) || assessment.getWeightKg() == null || assessment.getWeightKg() <= 0 || unanswered(assessment)) {
            flash.addFlashAttribute("error", "Please complete every health question with valid information."); return "redirect:/assessment";
        }
        assessment.setId(null); assessment.setUserId(donorId); assessment.setSubmittedAt(now);
        int score = 100;
        boolean ageOk = Period.between(assessment.getBirthDate(), LocalDate.now()).getYears() >= 18;
        if (!ageOk || assessment.getWeightKg() < 50) score -= 45;
        if (yes(assessment.getHasFever())) score -= 35;
        if (yes(assessment.getRecentSurgery()) || yes(assessment.getHighRiskExposure()) || yes(assessment.getPregnantOrBreastfeeding())) score -= 35;
        if (yes(assessment.getTakingMedication())) score -= 15;
        if (!yes(assessment.getFeelingWell())) score -= 25;
        assessment.setReadinessScore(Math.max(0, score));
        assessment.setEligibilityStatus(score >= 70 ? "ELIGIBLE" : "INELIGIBLE");
        assessment.setRetakeAllowedAt(score >= 70 ? null : now.plusHours(1));
        assessments.save(assessment);
        createNotification(donorId,"DONOR","Eligibility test " + assessment.getEligibilityStatus(),"Your latest eligibility test result is " + assessment.getEligibilityStatus() + ".","ELIGIBILITY_RESULT", "eligibility-donor-"+assessment.getId(),assessment.getId());
        User donor=users.findById(donorId).orElse(null);
        createNotification(null,"ADMIN","Donor eligibility test result",(donor==null?"Donor #"+donorId:donor.getFirstName()+" "+donor.getLastName())+" submitted an eligibility test: "+assessment.getEligibilityStatus()+".","ELIGIBILITY_RESULT","eligibility-admin-"+assessment.getId(),assessment.getId());
        if(assessment.getRetakeAllowedAt()!=null) createNotification(donorId,"DONOR","Eligibility test cooldown","You may retake the eligibility test after "+assessment.getRetakeAllowedAt()+" Philippine time.","ELIGIBILITY_COOLDOWN","eligibility-cooldown-"+assessment.getId(),assessment.getId());
        flash.addFlashAttribute("success", "Assessment saved. Your current status is " + assessment.getEligibilityStatus() + "."); return "redirect:/assessment";
    }
    private String review(HttpSession s, Model m) { m.addAttribute("admin", true); m.addAttribute("activePage", "assessment"); m.addAttribute("assessments", assessments.findAllByOrderBySubmittedAtDesc()); addProfile(s,m); return "assessment"; }
    private boolean unanswered(HealthAssessment a) { return blank(a.getFeelingWell())||blank(a.getHasFever())||blank(a.getTakingMedication())||blank(a.getRecentSurgery())||blank(a.getPregnantOrBreastfeeding())||blank(a.getHighRiskExposure()); }
    private boolean yes(String v){return "YES".equalsIgnoreCase(v);} private boolean blank(String v){return v==null||v.isBlank();}
    private boolean loggedIn(HttpSession s){return s.getAttribute("userId")!=null;} private boolean admin(HttpSession s){return "ADMIN".equals(RoleNames.canonical(String.valueOf(s.getAttribute("role"))));} private Long userId(HttpSession s){return ((Number)s.getAttribute("userId")).longValue();}
    private void createNotification(Long userId,String role,String title,String message,String type,String key,Long ref){if(!notifications.existsByEventKey(key)){var n=new controller.model.repository.Notification();n.setRecipientUserId(userId);n.setRecipientRole(role);n.setTitle(title);n.setMessage(message);n.setEventType(type);n.setEventKey(key);n.setReferenceId(ref);n.setCreatedAt(LocalDateTime.now(PHILIPPINE_TIME));notifications.save(n);}}
    private void addProfile(HttpSession s, Model m){m.addAttribute("firstName",s.getAttribute("firstName"));m.addAttribute("admin",admin(s));}
}
