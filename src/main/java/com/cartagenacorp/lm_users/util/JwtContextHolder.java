package com.cartagenacorp.lm_users.util;

import java.util.UUID;

public class JwtContextHolder {
    private static final ThreadLocal<UUID> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentToken = new ThreadLocal<>();
    private static final ThreadLocal<UUID> currentOrganizationId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentRole = new ThreadLocal<>();

    public static void setUserId(UUID userId) {
        currentUserId.set(userId);
    }

    public static UUID getUserId() {
        return currentUserId.get();
    }

    public static void setToken(String token) {
        currentToken.set(token);
    }

    public static String getToken() {
        return currentToken.get();
    }

    public static void setOrganizationId(UUID organizationId) {
        currentOrganizationId.set(organizationId);
    }

    public static UUID getOrganizationId() {
        return currentOrganizationId.get();
    }

    public static void setRole(String role) {
        currentRole.set(role);
    }

    public static String getRole() {
        return currentRole.get();
    }

    public static void clear() {
        currentUserId.remove();
        currentToken.remove();
        currentOrganizationId.remove();
        currentRole.remove();
    }
}

