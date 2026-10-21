package controller.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.springframework.ui.ExtendedModelMap;

import controller.model.repository.AppointmentRepository;
import controller.model.repository.BloodInventoryRepository;
import controller.model.repository.BloodRequestRepository;
import controller.model.repository.DonationRepository;
import controller.model.repository.HealthAssessmentRepository;
import controller.model.repository.UserRepository;
import jakarta.servlet.http.HttpSession;

class HomeControllerTest {
    @Test
    void rolePrefixedAdminReachesAdminDashboardWithEmptyDatabaseResults() {
        HomeController controller = new HomeController(
                repository(UserRepository.class), repository(AppointmentRepository.class),
                repository(HealthAssessmentRepository.class), repository(DonationRepository.class),
                repository(BloodRequestRepository.class), repository(BloodInventoryRepository.class));
        Map<String, Object> values = new HashMap<>();
        values.put("userId", 42L);
        values.put("role", "ROLE_ADMIN");
        HttpSession session = (HttpSession) Proxy.newProxyInstance(HttpSession.class.getClassLoader(),
                new Class<?>[] { HttpSession.class }, (proxy, method, args) -> {
                    if (method.getName().equals("getAttribute")) return values.get(args[0]);
                    if (method.getName().equals("setAttribute")) { values.put((String) args[0], args[1]); return null; }
                    return null;
                });
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("dashboard", controller.dashboard(session, model));
        assertEquals(Boolean.TRUE, model.get("admin"));
        assertEquals("ADMIN", model.get("role"));
        assertEquals("ADMIN", values.get("role"));
        assertEquals(0L, model.get("totalDonors"));
        assertTrue(HomeController.class.getResource("/templates/dashboard.html") != null,
                "The admin and donor dashboard view must exist on the classpath");
        Context viewContext = new Context(Locale.ROOT);
        model.asMap().forEach(viewContext::setVariable);
        String html = templateEngine().process("dashboard", viewContext);
        assertTrue(html.contains("LIFELINK ADMIN DASHBOARD"), "The admin view should render its actual dashboard content");
    }

    @Test
    void invalidRoleCannotOpenDashboard() {
        HomeController controller = new HomeController(
                repository(UserRepository.class), repository(AppointmentRepository.class),
                repository(HealthAssessmentRepository.class), repository(DonationRepository.class),
                repository(BloodRequestRepository.class), repository(BloodInventoryRepository.class));
        HttpSession session = (HttpSession) Proxy.newProxyInstance(HttpSession.class.getClassLoader(),
                new Class<?>[] { HttpSession.class }, (proxy, method, args) ->
                        method.getName().equals("getAttribute") ? "GUEST" : null);
        ExtendedModelMap model = new ExtendedModelMap();
        // A signed-in identity is present, but its role is not an authorized LifeLink role.
        HttpSession identifiedSession = (HttpSession) Proxy.newProxyInstance(HttpSession.class.getClassLoader(),
                new Class<?>[] { HttpSession.class }, (proxy, method, args) -> {
                    if (!method.getName().equals("getAttribute")) return null;
                    return "userId".equals(args[0]) ? 42L : "GUEST";
                });
        assertEquals("redirect:/login", controller.dashboard(identifiedSession, model));
    }

    @SuppressWarnings("unchecked")
    private static <T> T repository(Class<T> type) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, (proxy, method, args) -> {
            return switch (method.getName()) {
                case "findFirstByUserIdOrderBySubmittedAtDesc", "findById" -> Optional.empty();
                case "findByUserIdOrderByAppointmentDateAscAppointmentTimeAsc", "findAllByOrderByAppointmentDateAscAppointmentTimeAsc",
                        "findByUserIdAndStatusIgnoreCaseOrderByDonationDateDesc", "findAllByOrderByDonationDateDesc",
                        "findAllByOrderByCreatedAtDesc", "findByRequesterIdOrderByCreatedAtDesc",
                        "findByRoleIgnoreCaseOrderByLastNameAscFirstNameAsc" -> List.of();
                case "countByRoleIgnoreCase", "countByStatus", "countByStatusIgnoreCase", "countByStatusNot" -> 0L;
                default -> null;
            };
        });
    }

    private static TemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }
}
