package controller.model;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import controller.model.repository.User;
import controller.model.repository.UserRepository;

@Controller
public class RegisterController {

    private final UserRepository userRepository;

    // =====================================================
    // HARD-CODED ADMIN REGISTRATION CODE
    // =====================================================

    private static final String ADMIN_REGISTRATION_CODE =
            "LIFELINK-ADMIN-2026";


    public RegisterController(UserRepository userRepository) {

        this.userRepository = userRepository;

    }


    // =====================================================
    // SHOW REGISTER PAGE
    // =====================================================

    @GetMapping("/register")
    public String showRegisterPage() {

        return "register";

    }


    // =====================================================
    // REGISTER USER
    // =====================================================

    @PostMapping("/register")
    public String registerUser(
            User user,
            @RequestParam(required = false) String adminCode,
            Model model) {


        System.out.println(
                "===== REGISTER POST RECEIVED ====="
        );

        System.out.println(
                "Username: " + user.getUsername()
        );


        // =================================================
        // CHECK USERNAME
        // =================================================

        if (user.getUsername() == null ||
            user.getUsername().trim().isEmpty()) {

            model.addAttribute(
                    "error",
                    "Username is required."
            );

            return "register";

        }


        // =================================================
        // CHECK IF USERNAME ALREADY EXISTS
        // =================================================

        if (
            userRepository
                .findByUsername(user.getUsername())
                .isPresent()
        ) {

            System.out.println(
                    "===== USERNAME ALREADY EXISTS ====="
            );


            model.addAttribute(
                    "error",
                    "Username already exists."
            );


            return "register";

        }


        // =================================================
        // DETERMINE ACCOUNT TYPE
        // =================================================

        boolean usernameContainsNumber =
                user.getUsername().matches(".*\\d.*");


        // =================================================
        // ADMIN ACCOUNT
        // =================================================

        if (usernameContainsNumber) {


            System.out.println(
                    "===== NUMBER DETECTED IN USERNAME ====="
            );


            System.out.println(
                    "===== ADMIN CONFIRMATION REQUIRED ====="
            );


            // Check admin code

            if (
                adminCode == null ||
                !ADMIN_REGISTRATION_CODE.equals(adminCode)
            ) {


                System.out.println(
                        "===== INVALID ADMIN CODE ====="
                );


                model.addAttribute(
                        "error",
                        "Invalid Admin Registration Code."
                );


                return "register";

            }


            // Admin code is correct

            user.setRole("ADMIN");


            System.out.println(
                    "===== ADMIN CODE ACCEPTED ====="
            );


            System.out.println(
                    "===== ACCOUNT ROLE: ADMIN ====="
            );


        } else {


            // =================================================
            // DONOR ACCOUNT
            // =================================================

            user.setRole("DONOR");


            System.out.println(
                    "===== ACCOUNT ROLE: DONOR ====="
            );

        }


        // =================================================
        // SAVE USER
        // =================================================

        userRepository.save(user);


        System.out.println(
                "===== USER SAVED TO DATABASE ====="
        );


        System.out.println(
                "Role: " + user.getRole()
        );


        // =================================================
        // SUCCESS
        // =================================================

        model.addAttribute(
                "success",
                "Registration successful!"
        );


        return "register";

    }

}