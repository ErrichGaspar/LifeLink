package controller.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import controller.model.repository.User;
import controller.model.repository.UserRepository;

class RegisterControllerTest {
    private static final String ADMIN_CODE = "LIFELINK-ADMIN-2026";
    private UserRepository users;
    private RegisterController controller;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        controller = new RegisterController(users);
        when(users.findByUsername(anyString())).thenReturn(Optional.empty());
        when(users.saveAndFlush(any(User.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void usernameWithoutDigitsCreatesDonorWithoutCode() {
        User saved = submit("donorname", null);
        assertEquals("DONOR", saved.getRole());
        assertTrue(new BCryptPasswordEncoder().matches("LifeLink.123", saved.getPassword()));
        verify(users).saveAndFlush(saved);
    }

    @Test
    void numericUsernameWithExactCodeCreatesAdmin() {
        User saved = submit("donor123", ADMIN_CODE);
        assertEquals("ADMIN", saved.getRole());
        verify(users).saveAndFlush(saved);
    }

    @Test
    void verificationEndpointAcceptsOnlyTheExactAdminCode() {
        assertEquals("true", controller.verifyAdminCode(ADMIN_CODE).get("valid"));
        assertEquals("false", controller.verifyAdminCode("LIFELINK-ADMIN-2026 ").get("valid"));
    }

    @Test
    void verificationEndpointExplainsEmptyCode() {
        assertEquals("Enter the admin confirmation code to continue.",
                controller.verifyAdminCode("").get("message"));
    }

    @Test
    void codeDoesNotPromoteUsernameWithoutDigits() {
        User saved = submit("donorname", ADMIN_CODE);
        assertEquals("DONOR", saved.getRole());
    }

    @Test
    void numericUsernameWithoutCodeIsRejected() {
        ExtendedModelMap model = new ExtendedModelMap();
        String view = submit("donor123", null, model);
        assertEquals("register", view);
        assertTrue(String.valueOf(model.get("adminCodeError")).contains("Enter the admin confirmation code"));
        verify(users, never()).saveAndFlush(any(User.class));
    }

    @Test
    void numericUsernameWithEmptyCodeIsRejectedWithoutSaving() {
        ExtendedModelMap model = new ExtendedModelMap();
        String view = submit("donor123", "", model);
        assertEquals("register", view);
        assertTrue(String.valueOf(model.get("adminCodeError")).contains("Enter the admin confirmation code"));
        verify(users, never()).saveAndFlush(any(User.class));
    }

    @Test
    void numericUsernameWithIncorrectCodeIsRejected() {
        ExtendedModelMap model = new ExtendedModelMap();
        String view = submit("donor123", "wrong-code", model);
        assertEquals("register", view);
        assertEquals("The admin confirmation code is incorrect.", model.get("adminCodeError"));
        verify(users, never()).saveAndFlush(any(User.class));
    }

    @Test
    void duplicateUsernameIsRejectedWithoutSaving() {
        User existing = new User();
        existing.setUsername("donorname");
        when(users.findByUsername("donorname")).thenReturn(Optional.of(existing));
        ExtendedModelMap model = new ExtendedModelMap();
        String view = submit("donorname", null, model);
        assertEquals("register", view);
        assertEquals("Username already exists.", model.get("error"));
        verify(users, never()).saveAndFlush(any(User.class));
    }

    @Test
    void loginLoadsTheSavedAdminRoleFromTheDatabase() {
        User admin = new User(); admin.setId(7L); admin.setUsername("staff7");
        admin.setRole("ADMIN"); admin.setPassword(new BCryptPasswordEncoder().encode("LifeLink.123"));
        when(users.findByUsername("staff7")).thenReturn(Optional.of(admin));
        HttpSession session = mock(HttpSession.class);
        LoginController login = new LoginController(users);

        String result = login.login("staff7", "LifeLink.123", session, false, mock(HttpServletResponse.class), new ExtendedModelMap());

        assertEquals("redirect:/dashboard", result);
        verify(session, atLeastOnce()).setAttribute("role", "ADMIN");
    }

    private User submit(String username, String code) {
        ExtendedModelMap model = new ExtendedModelMap();
        String result = submit(username, code, model);
        assertEquals("redirect:/register", result);
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).saveAndFlush(saved.capture());
        return saved.getValue();
    }

    private String submit(String username, String code, ExtendedModelMap model) {
        User form = new User();
        form.setFirstName("Alex"); form.setLastName("Donor"); form.setUsername(username);
        form.setPassword("LifeLink.123"); form.setPhoneNumber("+639123456789"); form.setBloodType("O+");
        return controller.register(form, code, true, model, new RedirectAttributesModelMap());
    }
}
