package controller.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.spring6.SpringTemplateEngine;

class SidebarTemplateTest {
    private final TemplateEngine engine = createEngine();

    @Test
    void adminSidebarShowsAdminRoutesAndHighlightsCurrentPage() {
        String rendered = render(true, "records");

        assertTrue(rendered.contains("Donor Management"));
        assertTrue(rendered.contains("Donation Records"));
        assertTrue(rendered.contains("/records"));
        assertTrue(rendered.contains("aria-current=\"page\""));
        assertFalse(rendered.contains("My Blood Requests"));
    }

    @Test
    void donorSidebarShowsDonorRoutesAndHighlightsCurrentPage() {
        String rendered = render(false, "requests");

        assertTrue(rendered.contains("My Profile"));
        assertTrue(rendered.contains("Schedule a Donation"));
        assertTrue(rendered.contains("My Blood Requests"));
        assertTrue(rendered.contains("aria-current=\"page\""));
        assertFalse(rendered.contains("Donor Management"));
    }

    private String render(boolean admin, String activePage) {
        Context context = new Context(Locale.ROOT);
        context.setVariable("admin", admin);
        context.setVariable("activePage", activePage);
        context.setVariable("firstName", "LifeLink User");
        return engine.process("sidebar-host", context);
    }

    private TemplateEngine createEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);
        return templateEngine;
    }
}
