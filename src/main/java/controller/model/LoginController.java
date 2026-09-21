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
    public String showLoginPage() {
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

        session.setAttribute(
                "userId",
                user.getId()
        );

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
}