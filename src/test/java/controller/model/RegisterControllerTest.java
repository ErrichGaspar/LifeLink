package controller.model;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import controller.model.repository.User;
import controller.model.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class RegisterControllerTest {
    private static final String ADMIN_CODE = "LIFELINK-ADMIN-2026";
    private final Map<String, User> usersByUsername = new HashMap<>();
    private final List<User> savedUsers = new ArrayList<>();
    private UserRepository users;
    private RegisterController controller;

    @BeforeEach
    void setUp() {
        usersByUsername.clear();
        savedUsers.clear();
        users = repositoryProxy();
        controller = new RegisterController(users);
    }

    @Test
    void usernameWithoutDigitsCreatesDonorWithoutCode() {
        User saved = submit("donorname", null);
        assertEquals("DONOR", saved.getRole());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertTrue(encoder.matches("LifeLink.123", saved.getPassword()));
        assertFalse(encoder.matches("LifeLink.124", saved.getPassword()));
        assertNotEquals("LifeLink.123", saved.getPassword());
        assertEquals(List.of(saved), savedUsers);
    }

    @Test
    void numericUsernameWithExactCodeCreatesAdmin() {
        User saved = submit("donor123", ADMIN_CODE);
        assertEquals("ADMIN", saved.getRole());
        assertEquals(List.of(saved), savedUsers);
    }

    @Test
    void codeDoesNotPromoteUsernameWithoutDigits() {
        User saved = submit("donorname", ADMIN_CODE);
        assertEquals("DONOR", saved.getRole());
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
    void numericUsernameWithoutCodeIsRejectedWithoutSaving() {
        ExtendedModelMap model = new ExtendedModelMap();
        String view = submit("donor123", null, model);
        assertEquals("register", view);
        assertTrue(String.valueOf(model.get("adminCodeError")).contains("Enter the admin confirmation code"));
        assertTrue(savedUsers.isEmpty());
    }

    @Test
    void numericUsernameWithEmptyCodeIsRejectedWithoutSaving() {
        ExtendedModelMap model = new ExtendedModelMap();
        String view = submit("donor123", "", model);
        assertEquals("register", view);
        assertTrue(String.valueOf(model.get("adminCodeError")).contains("Enter the admin confirmation code"));
        assertTrue(savedUsers.isEmpty());
    }

    @Test
    void numericUsernameWithIncorrectCodeIsRejected() {
        ExtendedModelMap model = new ExtendedModelMap();
        String view = submit("donor123", "wrong-code", model);
        assertEquals("register", view);
        assertEquals("The admin confirmation code is incorrect.", model.get("adminCodeError"));
        assertTrue(savedUsers.isEmpty());
    }

    @Test
    void duplicateUsernameIsRejectedWithoutSaving() {
        User existing = new User();
        existing.setUsername("donorname");
        usersByUsername.put(existing.getUsername(), existing);
        ExtendedModelMap model = new ExtendedModelMap();
        String view = submit("donorname", null, model);
        assertEquals("register", view);
        assertEquals("Username already exists.", model.get("error"));
        assertTrue(savedUsers.isEmpty());
    }

    @Test
    void loginLoadsTheSavedAdminRoleFromTheDatabase() {
        User admin = new User();
        admin.setId(7L);
        admin.setUsername("staff7");
        admin.setRole("ADMIN");
        admin.setPassword(new BCryptPasswordEncoder().encode("LifeLink.123"));
        usersByUsername.put(admin.getUsername(), admin);
        HttpSession session = (HttpSession) Proxy.newProxyInstance(HttpSession.class.getClassLoader(),
                new Class<?>[] { HttpSession.class }, (proxy, method, args) -> null);
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
                new Class<?>[] { HttpServletRequest.class }, (proxy, method, args) -> false);
        LoginController login = new LoginController(users);

        String result = login.login("staff7", "LifeLink.123", session, false, request,
                (HttpServletResponse) Proxy.newProxyInstance(HttpServletResponse.class.getClassLoader(),
                        new Class<?>[] { HttpServletResponse.class }, (proxy, method, args) -> null),
                new ExtendedModelMap());

        assertEquals("redirect:/dashboard", result);
    }

    @Test
    void loginAcceptsSpringPrefixedAdminRoleAndStoresCanonicalSessionRole() {
        User admin = new User();
        admin.setId(8L);
        admin.setUsername("prefixed-admin");
        admin.setRole("ROLE_ADMIN");
        admin.setPassword(new BCryptPasswordEncoder().encode("LifeLink.123"));
        usersByUsername.put(admin.getUsername(), admin);
        Map<String, Object> sessionValues = new HashMap<>();
        HttpSession session = (HttpSession) Proxy.newProxyInstance(HttpSession.class.getClassLoader(),
                new Class<?>[] { HttpSession.class }, (proxy, method, args) -> {
                    if (method.getName().equals("setAttribute")) { sessionValues.put((String) args[0], args[1]); return null; }
                    if (method.getName().equals("getAttribute")) return sessionValues.get(args[0]);
                    return null;
                });
        LoginController login = new LoginController(users);

        String result = login.login("prefixed-admin", "LifeLink.123", session, false, request(false),
                (HttpServletResponse) Proxy.newProxyInstance(HttpServletResponse.class.getClassLoader(),
                        new Class<?>[] { HttpServletResponse.class }, (proxy, method, args) -> null),
                new ExtendedModelMap());

        assertEquals("redirect:/dashboard", result);
        assertEquals("ADMIN", sessionValues.get("role"));
    }

    @Test
    void rememberMeCookieIsPersistentAndLogoutInvalidatesItsToken() {
        User donor = new User();
        donor.setId(12L);
        donor.setUsername("remembered");
        donor.setRole("DONOR");
        donor.setPassword(new BCryptPasswordEncoder().encode("LifeLink.123"));
        usersByUsername.put(donor.getUsername(), donor);
        Map<String, Object> sessionValues = new HashMap<>();
        List<String> setCookieHeaders = new ArrayList<>();
        boolean[] invalidated = { false };
        HttpSession session = (HttpSession) Proxy.newProxyInstance(HttpSession.class.getClassLoader(),
                new Class<?>[] { HttpSession.class }, (proxy, method, args) -> {
                    if (method.getName().equals("getAttribute")) return sessionValues.get(args[0]);
                    if (method.getName().equals("setAttribute")) { sessionValues.put((String) args[0], args[1]); return null; }
                    if (method.getName().equals("invalidate")) { invalidated[0] = true; sessionValues.clear(); return null; }
                    return null;
                });
        HttpServletRequest request = request(true);
        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(HttpServletResponse.class.getClassLoader(),
                new Class<?>[] { HttpServletResponse.class }, (proxy, method, args) -> {
                    if (method.getName().equals("addHeader") && "Set-Cookie".equals(args[0])) setCookieHeaders.add((String) args[1]);
                    return null;
                });
        LoginController login = new LoginController(users);

        assertEquals("redirect:/dashboard", login.login("remembered", "LifeLink.123", session, true,
                request, response, new ExtendedModelMap()));
        assertNotNull(donor.getRememberToken());
        assertTrue(setCookieHeaders.get(0).contains("Max-Age=2592000"));
        assertTrue(setCookieHeaders.get(0).contains("HttpOnly"));
        assertTrue(setCookieHeaders.get(0).contains("Secure"));
        assertTrue(setCookieHeaders.get(0).contains("SameSite=Lax"));

        assertEquals("redirect:/", login.logout(session, request, response));
        assertTrue(invalidated[0]);
        assertNull(donor.getRememberToken());
        assertTrue(setCookieHeaders.get(1).contains("Max-Age=0"));
    }

    private HttpServletRequest request(boolean secure) {
        return (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
                new Class<?>[] { HttpServletRequest.class }, (proxy, method, args) -> method.getName().equals("isSecure") && secure);
    }

    private User submit(String username, String code) {
        ExtendedModelMap model = new ExtendedModelMap();
        String result = submit(username, code, model);
        assertEquals("redirect:/register", result);
        assertEquals(1, savedUsers.size());
        return savedUsers.get(0);
    }

    private String submit(String username, String code, ExtendedModelMap model) {
        User form = new User();
        form.setFirstName("Alex");
        form.setLastName("Donor");
        form.setUsername(username);
        form.setPassword("LifeLink.123");
        form.setPhoneNumber("+639123456789");
        form.setBloodType("O+");
        return controller.register(form, code, true, model, new RedirectAttributesModelMap());
    }

    private UserRepository repositoryProxy() {
        return (UserRepository) Proxy.newProxyInstance(UserRepository.class.getClassLoader(),
                new Class<?>[] { UserRepository.class }, (proxy, method, args) -> {
                    return switch (method.getName()) {
                        case "findByUsername" -> Optional.ofNullable(usersByUsername.get((String) args[0]));
                        case "findById" -> usersByUsername.values().stream().filter(user -> user.getId() != null && user.getId().equals(args[0])).findFirst();
                        case "saveAndFlush", "save" -> {
                            User user = (User) args[0];
                            usersByUsername.put(user.getUsername(), user);
                            savedUsers.add(user);
                            yield user;
                        }
                        case "toString" -> "In-memory UserRepository test double";
                        case "hashCode" -> System.identityHashCode(proxy);
                        case "equals" -> proxy == args[0];
                        default -> null;
                    };
                });
    }
}
