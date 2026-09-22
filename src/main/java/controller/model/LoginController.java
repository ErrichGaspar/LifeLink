package controller.model;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import controller.model.repository.User;
import controller.model.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
public class LoginController {

    private final UserRepository userRepository;

    public LoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    // =====================================================
    // SHOW LOGIN PAGE
    // =====================================================

    @GetMapping("/login")
    public String showLoginPage(HttpServletRequest request, HttpSession session) {
        if (session.getAttribute("userId") != null) return "redirect:/dashboard";
        if (request.getCookies() != null) for (Cookie cookie : request.getCookies()) {
            if ("lifelink_remember".equals(cookie.getName())) {
                Optional<User> user = userRepository.findByRememberToken(cookie.getValue());
                if (user.isPresent()) { startSession(session, user.get()); return "redirect:/dashboard"; }
            }
        }
        return "login";
    }


    // =====================================================
    // LOGIN
    // =====================================================

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            @RequestParam(required = false) boolean remember,
            HttpServletResponse response,
            Model model) {

        // Find user by username
        Optional<User> userOptional =
                userRepository.findByUsername(username);


        // =================================================
        // CHECK USERNAME AND PASSWORD
        // =================================================

        if (userOptional.isEmpty()
                || !userOptional.get().getPassword().equals(password)) {

            model.addAttribute(
                    "error",
                    "Invalid username or password."
            );

            return "login";
        }


        // Get the actual user from database
        User user = userOptional.get();


        // =================================================
        // CHECK ROLE
        // =================================================

        String role = user.getRole();


        // Safety check in case an old account has no role
        if (role == null || role.isBlank()) {

            role = "DONOR";

            user.setRole(role);

            userRepository.save(user);
        }


        // =================================================
        // SAVE USER INFORMATION IN SESSION
        // =================================================

        startSession(session, user);

        if (remember) {
            String token = UUID.randomUUID().toString();
            user.setRememberToken(token); userRepository.save(user);
            Cookie cookie = new Cookie("lifelink_remember", token);
            cookie.setHttpOnly(true); cookie.setPath("/"); cookie.setMaxAge(60 * 60 * 24 * 30);
            response.addCookie(cookie);
        }

        session.setAttribute(
                "firstName",
                user.getFirstName()
        );

        session.setAttribute(
                "lastName",
                user.getLastName()
        );

        session.setAttribute(
                "role",
                role
        );

        session.setAttribute(
                "bloodType",
                user.getBloodType()
        );


        // =================================================
        // LOGIN SUCCESSFUL
        // =================================================

        System.out.println("===== LOGIN SUCCESS =====");
        System.out.println("Username: " + user.getUsername());
        System.out.println("Role: " + role);


        // Both donor and admin currently go to
        // /dashboard.
        //
        // The dashboard controller will later use
        // the session role to show the correct dashboard.

        return "redirect:/dashboard";
    }


    // =====================================================
    // LOGOUT
    // =====================================================

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/";
    }

    private void startSession(HttpSession session, User user) {
        session.setAttribute("userId", user.getId());
        session.setAttribute("firstName", user.getFirstName());
        session.setAttribute("lastName", user.getLastName());
        session.setAttribute("role", user.getRole());
        session.setAttribute("bloodType", user.getBloodType());
    }
}
