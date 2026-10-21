package controller.model;

import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import controller.model.repository.*;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
    private static final List<String> TYPES=List.of("A+","A-","B+","B-","AB+","AB-","O+","O-");
    private final UserRepository users; private final AppointmentRepository appointments; private final HealthAssessmentRepository assessments;
    private final DonationRepository donations; private final BloodRequestRepository requests; private final BloodInventoryRepository inventory;
    public HomeController(UserRepository u,AppointmentRepository a,HealthAssessmentRepository h,DonationRepository d,BloodRequestRepository r,BloodInventoryRepository i){users=u;appointments=a;assessments=h;donations=d;requests=r;inventory=i;}
    @GetMapping("/") public String home(){return "index";}
    @GetMapping("/learn-more") public String learnMore(){return "learn-more";}
    @GetMapping("/dashboard") public String dashboard(HttpSession s,Model m){
        Object sessionUser=s.getAttribute("userId"); if(sessionUser==null)return "redirect:/login";
        Long userId=((Number)sessionUser).longValue(); String role=String.valueOf(s.getAttribute("role")); boolean admin="ADMIN".equalsIgnoreCase(role);
        if (!admin && !"DONOR".equalsIgnoreCase(role)) return "redirect:/login";
        m.addAttribute("firstName",s.getAttribute("firstName"));m.addAttribute("lastName",s.getAttribute("lastName"));m.addAttribute("role",role);m.addAttribute("bloodType",s.getAttribute("bloodType"));m.addAttribute("admin",admin);m.addAttribute("activePage","dashboard");
        var userAppointments=appointments.findByUserIdOrderByAppointmentDateAscAppointmentTimeAsc(userId);
        m.addAttribute("upcomingAppointment",userAppointments.stream().filter(a->a.getAppointmentDate()!=null&&!a.getAppointmentDate().isBefore(LocalDate.now())&&List.of("UPCOMING","CONFIRMED").contains(String.valueOf(a.getStatus()).toUpperCase())).findFirst().orElse(null));
        HealthAssessment latestAssessment=assessments.findFirstByUserIdOrderBySubmittedAtDesc(userId).orElse(null);
        List<Donation> donorHistory=donations.findByUserIdAndStatusIgnoreCaseOrderByDonationDateDesc(userId,"COMPLETED");
        m.addAttribute("assessment",latestAssessment);m.addAttribute("donationHistory",donorHistory);m.addAttribute("totalDonations",donorHistory.size());
        LocalDate nextEligible=donorHistory.stream().map(d->d.getNextEligibleDate()!=null?d.getNextEligibleDate():(d.getDonationDate()==null?null:d.getDonationDate().plusDays(90))).filter(Objects::nonNull).findFirst().orElse(null);
        String eligibility=latestAssessment==null||latestAssessment.getReadinessScore()==null?"In Process":latestAssessment.getReadinessScore()>=70?"ELIGIBLE":latestAssessment.getReadinessScore()>=45?"IN PROCESS":"NOT ELIGIBLE";
        m.addAttribute("currentEligibility",nextEligible!=null&&nextEligible.isAfter(LocalDate.now())?"IN PROCESS":eligibility);
        m.addAttribute("bloodRequests",admin?requests.findAllByOrderByCreatedAtDesc():requests.findByRequesterIdOrderByCreatedAtDesc(userId));
        if(admin){
            List<Appointment> upcoming=appointments.findAllByOrderByAppointmentDateAscAppointmentTimeAsc().stream().filter(a->a.getAppointmentDate()!=null&&!a.getAppointmentDate().isBefore(LocalDate.now())&&List.of("UPCOMING","CONFIRMED").contains(String.valueOf(a.getStatus()).toUpperCase())).toList();
            Map<String,Integer> byType=new LinkedHashMap<>();for(String type:TYPES)byType.put(type,inventory.findById(type).map(b->Math.max(0,b.getUnits()==null?0:b.getUnits())).orElse(0));
            m.addAttribute("inventoryMax",byType.values().stream().mapToInt(Integer::intValue).max().orElse(0));
            Map<Long,String> donorNames=new HashMap<>();for(User donor:users.findByRoleIgnoreCaseOrderByLastNameAscFirstNameAsc("DONOR"))donorNames.put(donor.getId(),donor.getFirstName()+" "+donor.getLastName());
            m.addAttribute("totalDonors",users.countByRoleIgnoreCase("DONOR"));m.addAttribute("totalCompletedDonations",donations.countByStatus("COMPLETED"));
            m.addAttribute("upcomingAppointments",upcoming.size());m.addAttribute("allAppointments",upcoming.stream().limit(8).toList());m.addAttribute("donorNames",donorNames);
            m.addAttribute("pendingRequests",requests.countByStatusIgnoreCase("PENDING"));m.addAttribute("approvedRequests",requests.countByStatusIgnoreCase("APPROVED"));
            m.addAttribute("activeRequests",requests.countByStatusNot("FULFILLED"));m.addAttribute("inventoryByType",byType);m.addAttribute("recentDonations",donations.findAllByOrderByDonationDateDesc().stream().filter(d->"COMPLETED".equalsIgnoreCase(d.getStatus())).limit(5).toList());
        }
        return "dashboard";
    }
}
