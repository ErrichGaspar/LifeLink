package controller.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import controller.model.repository.*;
import jakarta.servlet.http.HttpSession;

@Controller
public class OperationsController {
    private static final List<String> TYPES = List.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");
    private final DonationRepository donations; private final BloodRequestRepository requests;
    private final BloodInventoryRepository inventory; private final UserRepository users;
    private final AppointmentRepository appointments;
    private final InventoryMovementRepository movements;
    @Value("${lifelink.requests.auto-approve:false}") private boolean automaticApprovalEnabled;
    public OperationsController(DonationRepository d, BloodRequestRepository r, BloodInventoryRepository i, UserRepository u, AppointmentRepository a, InventoryMovementRepository movements) { donations=d; requests=r; inventory=i; users=u; appointments=a; this.movements=movements; }

    @GetMapping("/records") public String records(HttpSession s, Model m) {
        if(!admin(s)) return "redirect:/dashboard";
        m.addAttribute("donation",new Donation()); m.addAttribute("donors",users.findByRoleIgnoreCaseOrderByLastNameAscFirstNameAsc("DONOR")); Map<Long,String> donorNames=new java.util.HashMap<>();users.findByRoleIgnoreCaseOrderByLastNameAscFirstNameAsc("DONOR").forEach(u->donorNames.put(u.getId(),u.getDonorId()+" · "+u.getFirstName()+" "+u.getLastName()));m.addAttribute("donorNames",donorNames);
        m.addAttribute("donations",donations.findAllByOrderByDonationDateDesc()); m.addAttribute("activePage","records"); add(s,m); return "records";
    }
    @PostMapping("/records") @Transactional public String record(Donation d,HttpSession s,RedirectAttributes f) {
        if(!admin(s))return "redirect:/dashboard";
        User donor=d.getUserId()==null?null:users.findByIdForUpdate(d.getUserId()).filter(u->"DONOR".equalsIgnoreCase(u.getRole())).orElse(null);
        if(donor==null||d.getDonationDate()==null||d.getDonationDate().isAfter(LocalDate.now())||d.getUnits()==null||d.getUnits()<1||d.getUnits()>10||!validType(donor.getBloodType())){f.addFlashAttribute("error","Choose a donor, a valid past or current date, and 1–10 collected bags.");return "redirect:/records";}
        if(donations.existsByUserIdAndDonationDate(donor.getId(),d.getDonationDate())){f.addFlashAttribute("error","This donor already has a donation recorded for that date.");return "redirect:/records";}
        d.setId(null);d.setAppointmentId(null);d.setBloodType(donor.getBloodType());d.setStatus("COMPLETED");d.setNextEligibleDate(d.getDonationDate().plusDays(90));d.setInventoryAccepted(false);donations.save(d);
        f.addFlashAttribute("success","Donation recorded in history. Collected bags are awaiting inventory acceptance.");return "redirect:/records";
    }
    @PostMapping("/records/{id}/accept") @Transactional public String acceptDonation(@PathVariable Long id,HttpSession s,RedirectAttributes f) {
        if(!admin(s))return "redirect:/dashboard";
        Donation d=donations.findByIdForUpdate(id).orElse(null);
        if(d==null||!"COMPLETED".equalsIgnoreCase(d.getStatus())||d.getUnits()==null||d.getUnits()<1||!validType(d.getBloodType())){f.addFlashAttribute("error","Donation cannot be accepted into inventory.");return "redirect:/records";}
        if(Boolean.TRUE.equals(d.getInventoryAccepted())){f.addFlashAttribute("success","These bags were already accepted into inventory.");return "redirect:/records";}
        BloodInventory b=inventory.findByTypeForUpdate(d.getBloodType()).orElseGet(()->{BloodInventory n=new BloodInventory();n.setBloodType(d.getBloodType());n.setUnits(0);return n;});
        b.setUnits(Math.max(0,b.getUnits()==null?0:b.getUnits())+d.getUnits()); inventory.save(b); d.setInventoryAccepted(true); donations.save(d);
        InventoryMovement movement=new InventoryMovement();movement.setBloodType(d.getBloodType());movement.setQuantity(d.getUnits());movement.setMovementType("ACCEPTED");movement.setReferenceType("DONATION");movement.setReferenceId(d.getId());movement.setLocation(d.getLocation());movement.setCreatedAt(LocalDateTime.now());movements.save(movement);
        f.addFlashAttribute("success","Collected bags accepted into available inventory.");return "redirect:/records";
    }
    @PostMapping("/records/{id}/status") public String donationStatus(@PathVariable Long id,@RequestParam String status,HttpSession s,RedirectAttributes f){if(!admin(s))return "redirect:/dashboard";f.addFlashAttribute("error","Donation status is derived from collection and inventory acceptance; it cannot be edited directly.");return "redirect:/records";}

    @GetMapping("/requests") public String requestList(HttpSession s,Model m){if(!logged(s))return "redirect:/login";boolean isAdmin=admin(s);m.addAttribute("admin",isAdmin);m.addAttribute("activePage","requests");m.addAttribute("request",new BloodRequest());m.addAttribute("requests",isAdmin?requests.findAllByOrderByCreatedAtDesc():requests.findByRequesterIdOrderByCreatedAtDesc(uid(s)));m.addAttribute("bloodTypes",TYPES);add(s,m);return "requests";}
    @PostMapping("/requests") @Transactional public String createRequest(BloodRequest r,HttpSession s,RedirectAttributes f){
        if(!logged(s))return "redirect:/login";
        String type=r.getBloodType()==null?"":r.getBloodType().trim().toUpperCase();
        if(blank(r.getPatientName())||r.getPatientName().length()>120||blank(r.getHospital())||r.getHospital().length()>160||!validType(type)||r.getUnits()==null||r.getUnits()<1||r.getUnits()>100||r.getNeededDate()==null||r.getNeededDate().isBefore(LocalDate.now())||r.getReason()!=null&&r.getReason().length()>500){f.addFlashAttribute("error","Enter a patient, hospital, supported blood type, 1–100 bags, a valid needed date, and a reason no longer than 500 characters.");return "redirect:/requests";}
        r.setPatientName(r.getPatientName().trim());r.setHospital(r.getHospital().trim());if(r.getReason()!=null)r.setReason(r.getReason().trim());
        r.setId(null);r.setDispatchedAt(null);r.setDispatchedUnits(null);r.setScheduledDispatchDate(null);r.setDeliveryDate(null);r.setDispatchNotes(null);r.setBloodType(type);r.setPriority("URGENT".equalsIgnoreCase(r.getPriority())?"URGENT":"NORMAL");r.setCreatedAt(LocalDateTime.now());
        r.setRequester((s.getAttribute("firstName")==null?"":s.getAttribute("firstName")+" ")+String.valueOf(s.getAttribute("lastName") == null ? "" : s.getAttribute("lastName")));r.setRequesterId(uid(s));
        int available=inventory.findById(type).map(b->Math.max(0,b.getUnits()==null?0:b.getUnits())).orElse(0);
        r.setEvaluation(available>=r.getUnits()?"Inventory currently sufficient; admin review required.":"Insufficient inventory at submission; request remains pending for later review.");
        boolean auto=automaticApprovalEnabled;
        boolean autoApproved=auto&&available>=r.getUnits();r.setStatus(autoApproved?"APPROVED":"PENDING");if(autoApproved)r.setEvaluation("Automatically approved under configured policy; inventory will be deducted only at dispatch.");requests.save(r);f.addFlashAttribute("success","Blood request submitted"+(autoApproved?" and approved under the configured policy.":" for review."));return "redirect:/requests";
    }
    @PostMapping("/requests/{id}/status") @Transactional public String requestStatus(@PathVariable Long id,@RequestParam String status,@RequestParam(required=false) LocalDate scheduledDispatchDate,@RequestParam(required=false) LocalDate deliveryDate,@RequestParam(required=false) Integer dispatchUnits,@RequestParam(required=false) String dispatchNotes,HttpSession s,RedirectAttributes f){
        if(!admin(s))return "redirect:/dashboard"; BloodRequest r=requests.findByIdForUpdate(id).orElse(null);if(r==null){f.addFlashAttribute("error","Request not found.");return "redirect:/requests";}
        String next=status==null?"":status.toUpperCase();
        if("APPROVED".equals(next)){
            if(blank(r.getHospital())||blank(r.getPatientName())||!validType(r.getBloodType())||r.getUnits()==null||r.getUnits()<1||r.getNeededDate()==null||r.getNeededDate().isBefore(LocalDate.now())){r.setEvaluation("Request information is invalid or its needed date has passed. Please review the request.");requests.save(r);f.addFlashAttribute("error",r.getEvaluation());return "redirect:/requests";}
            int available=inventory.findById(r.getBloodType()).map(b->Math.max(0,b.getUnits()==null?0:b.getUnits())).orElse(0);
            if(available<r.getUnits()){r.setEvaluation("Insufficient available inventory. Request remains pending and may be reviewed later.");requests.save(r);f.addFlashAttribute("error",r.getEvaluation());return "redirect:/requests";}
            r.setStatus("APPROVED");r.setEvaluation("Approved after administrator review; inventory will be deducted only at dispatch.");
        } else if("REJECTED".equals(next)){r.setStatus(next);r.setEvaluation("Rejected after administrator review.");}
        else if("SCHEDULED FOR DISPATCH".equals(next)||"SCHEDULED_FOR_DISPATCH".equals(next)){
            int quantity=dispatchUnits==null?r.getUnits():dispatchUnits;
            if(!"APPROVED".equalsIgnoreCase(r.getStatus())||scheduledDispatchDate==null||scheduledDispatchDate.isBefore(LocalDate.now())||r.getNeededDate()!=null&&scheduledDispatchDate.isAfter(r.getNeededDate())||deliveryDate!=null&&(deliveryDate.isBefore(scheduledDispatchDate)||r.getNeededDate()!=null&&deliveryDate.isAfter(r.getNeededDate()))||quantity<1||quantity>r.getUnits()) {f.addFlashAttribute("error","Approve first, then enter valid dispatch and delivery dates no later than the requested date, and a quantity up to the request amount.");return "redirect:/requests";}
            r.setScheduledDispatchDate(scheduledDispatchDate);r.setDeliveryDate(deliveryDate);r.setDispatchedUnits(quantity);r.setDispatchNotes(dispatchNotes);r.setStatus("SCHEDULED_FOR_DISPATCH");
        } else if("DISPATCHED".equals(next)){
            if(!"SCHEDULED_FOR_DISPATCH".equalsIgnoreCase(r.getStatus())||r.getDispatchedAt()!=null){f.addFlashAttribute("error","Only a scheduled request can be dispatched, and each request can be dispatched once.");return "redirect:/requests";}
            int quantity=r.getDispatchedUnits()==null?0:r.getDispatchedUnits(); BloodInventory b=inventory.findByTypeForUpdate(r.getBloodType()).orElse(null);
            if(b==null||quantity<1||b.getUnits()==null||b.getUnits()<quantity){f.addFlashAttribute("error","Available inventory is now insufficient; no bags were dispatched.");return "redirect:/requests";}
            b.setUnits(b.getUnits()-quantity);inventory.save(b);r.setDispatchedAt(LocalDateTime.now());r.setStatus("DISPATCHED");r.setEvaluation("Dispatched " + quantity + " bags.");
            InventoryMovement movement=new InventoryMovement();movement.setBloodType(r.getBloodType());movement.setQuantity(-quantity);movement.setMovementType("DISPATCHED");movement.setReferenceType("BLOOD_REQUEST");movement.setReferenceId(r.getId());movement.setLocation(r.getHospital());movement.setCreatedAt(LocalDateTime.now());movements.save(movement);
        } else if("FULFILLED".equals(next)&&"DISPATCHED".equalsIgnoreCase(r.getStatus()))r.setStatus("FULFILLED");
        else {f.addFlashAttribute("error","That request status transition is not valid.");return "redirect:/requests";}
        requests.save(r);f.addFlashAttribute("success","Blood request updated.");return "redirect:/requests";
    }
    @GetMapping("/reports") public String reports(HttpSession s,Model m){if(!logged(s))return "redirect:/login";boolean isAdmin=admin(s);m.addAttribute("admin",isAdmin);m.addAttribute("activePage","reports");if(isAdmin){List<User> donorList=users.findByRoleIgnoreCaseOrderByLastNameAscFirstNameAsc("DONOR");Map<Long,LocalDate> lastDonation=new java.util.HashMap<>();for(User donor:donorList)donations.findByUserIdAndStatusIgnoreCaseOrderByDonationDateDesc(donor.getId(),"COMPLETED").stream().map(Donation::getDonationDate).filter(java.util.Objects::nonNull).findFirst().ifPresent(date->lastDonation.put(donor.getId(),date));m.addAttribute("donors",donorList);m.addAttribute("lastDonation",lastDonation);}else{m.addAttribute("donations",donations.findByUserIdAndStatusIgnoreCaseOrderByDonationDateDesc(uid(s),"COMPLETED"));}add(s,m);return "reports";}
    @GetMapping("/reports/donor/{id}") public String donorHistory(@PathVariable Long id,HttpSession s,Model m,RedirectAttributes f){if(!admin(s))return "redirect:/dashboard";User donor=users.findById(id).filter(u->"DONOR".equalsIgnoreCase(u.getRole())).orElse(null);if(donor==null){f.addFlashAttribute("error","Donor not found.");return "redirect:/reports";}m.addAttribute("admin",true);m.addAttribute("activePage","reports");m.addAttribute("donor",donor);m.addAttribute("donations",donations.findByUserIdAndStatusIgnoreCaseOrderByDonationDateDesc(id,"COMPLETED"));add(s,m);return "donor-history";}
    private boolean validType(String type){return type!=null&&TYPES.contains(type.toUpperCase());}
    private Long uid(HttpSession s){return ((Number)s.getAttribute("userId")).longValue();}
    private boolean logged(HttpSession s){return s.getAttribute("userId")!=null;}private boolean admin(HttpSession s){return "ADMIN".equalsIgnoreCase(String.valueOf(s.getAttribute("role")));}private boolean blank(String v){return v==null||v.isBlank();}private void add(HttpSession s,Model m){m.addAttribute("firstName",s.getAttribute("firstName"));m.addAttribute("admin",admin(s));}
}
