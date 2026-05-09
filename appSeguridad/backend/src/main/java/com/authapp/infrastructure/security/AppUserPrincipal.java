package com.authapp.infrastructure.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class AppUserPrincipal {
    private Long id;
    private String email;
    private List<String> roles;
    private List<String> permissions;

    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    public boolean isAdmin() {
        return roles != null && roles.contains("ADMIN");
    }
}
