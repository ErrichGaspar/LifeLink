package controller.model;

import java.util.Locale;

/** Normalizes the role values used by stored accounts and session-based authorization. */
public final class RoleNames {
    private RoleNames() { }

    public static String canonical(String role) {
        if (role == null) return null;
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("ROLE_")) normalized = normalized.substring("ROLE_".length());
        return "ADMIN".equals(normalized) || "DONOR".equals(normalized) ? normalized : null;
    }
}
