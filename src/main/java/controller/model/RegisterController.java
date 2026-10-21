package controller.model;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;

import controller.model.repository.User;
import controller.model.repository.UserRepository;

@Controller
public class RegisterController {
    private static final String ADMIN_CONFIRMATION_CODE = "LIFELINK-ADMIN-2026";
    private final UserRepository users;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    public RegisterController(UserRepository users) { this.users = users; }

    @GetMapping("/register") public String page() { return "register"; }

    @PostMapping("/register/verify-admin-code")
    @ResponseBody
    public Map<String, String> verifyAdminCode(@RequestParam(required = false) String adminCode) {
        if (ADMIN_CONFIRMATION_CODE.equals(adminCode)) return Map.of("valid", "true");
        return Map.of("valid", "false", "message", adminCode == null || adminCode.isBlank()
                ? "Enter the admin confirmation code to continue."
                : "The admin confirmation code is incorrect.");
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("form") User form, @RequestParam(required=false) String adminCode, @RequestParam(required=false) Boolean terms, Model model, RedirectAttributes flash) {
        String username = clean(form.getUsername());
        String first = clean(form.getFirstName()); String last = clean(form.getLastName());
        String blood = clean(form.getBloodType()).toUpperCase(); String phone = clean(form.getPhoneNumber());
        String password = form.getPassword() == null ? "" : form.getPassword();
        boolean numericUsername = username.chars().anyMatch(Character::isDigit);
        model.addAttribute("numericUsername", numericUsername);
        if (!Boolean.TRUE.equals(terms) || !username.matches("^[A-Za-z0-9._]{3,40}$") || !first.matches("^[\\p{L}]+(?:[ '-][\\p{L}]+)*$") || first.length()>60 || !last.matches("^[\\p{L}]+(?:[ '-][\\p{L}]+)*$") || last.length()>60 || !phone.matches("^\\+639\\d{9}$") || !blood.matches("^(A|B|AB|O)[+-]$")
                || !password.matches("(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[._])[^\\s]{8,30}")) {
            model.addAttribute("error", "Enter your name, username, supported blood type, and a password with 8–30 characters including upper/lowercase letters, a number, and . or _.");
            model.addAttribute("form", form);
            return "register";
        }
        if (users.findByUsername(username).isPresent()) { model.addAttribute("error", "Username already exists."); model.addAttribute("form", form); return "register"; }
        if (numericUsername && (adminCode == null || !ADMIN_CONFIRMATION_CODE.equals(adminCode))) {
            model.addAttribute("adminCodeError", adminCode == null || adminCode.isBlank()
                    ? "Your username contains a number. Enter the admin confirmation code to continue."
                    : "The admin confirmation code is incorrect.");
            model.addAttribute("form", form);
            return "register";
        }
        form.setFirstName(first); form.setLastName(last); form.setUsername(username); form.setBloodType(blood);
        form.setPhoneNumber(phone); form.setRole(numericUsername ? "ADMIN" : "DONOR"); form.setPassword(encoder.encode(password));
        form.setId(null); form.setRememberToken(null);
        try { users.saveAndFlush(form); }
        catch (RuntimeException ex) { model.addAttribute("error", "We could not create the account. Check the information and try again."); model.addAttribute("form", form); return "register"; }
        flash.addFlashAttribute("success", "Account created successfully!");
        return "redirect:/register";
    }
    private String clean(String value) { return value == null ? "" : value.trim(); }
}
