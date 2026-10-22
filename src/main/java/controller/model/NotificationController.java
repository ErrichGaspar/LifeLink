package controller.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import controller.model.repository.*;
import jakarta.servlet.http.HttpSession;

@Controller
public class NotificationController {
    private final NotificationRepository notifications;
    public NotificationController(NotificationRepository notifications){this.notifications=notifications;}
    @GetMapping("/notifications") public String page(HttpSession s, Model m){
        if(s.getAttribute("userId")==null)return "redirect:/login";
        boolean isAdmin="ADMIN".equals(RoleNames.canonical(String.valueOf(s.getAttribute("role"))));
        Long uid=((Number)s.getAttribute("userId")).longValue();
        List<Notification> items=isAdmin?notifications.findByRecipientRoleAndRecipientUserIdIsNullOrderByCreatedAtDesc("ADMIN"):notifications.findByRecipientRoleAndRecipientUserIdOrderByCreatedAtDesc("DONOR",uid);
        m.addAttribute("notifications",items);m.addAttribute("admin",isAdmin);m.addAttribute("firstName",s.getAttribute("firstName"));m.addAttribute("activePage","notifications");return "notifications";
    }
    @PostMapping("/notifications/{id}/read") public String markRead(@PathVariable Long id,HttpSession s){
        if(s.getAttribute("userId")==null)return "redirect:/login";
        boolean isAdmin="ADMIN".equals(RoleNames.canonical(String.valueOf(s.getAttribute("role"))));Long uid=((Number)s.getAttribute("userId")).longValue();
        notifications.findById(id).filter(n->isAdmin?"ADMIN".equals(n.getRecipientRole())&&n.getRecipientUserId()==null:"DONOR".equals(n.getRecipientRole())&&uid.equals(n.getRecipientUserId())).ifPresent(n->{if(n.getReadAt()==null){n.setReadAt(LocalDateTime.now(ZoneId.of("Asia/Manila")));notifications.save(n);}});
        return "redirect:/notifications";
    }
}
