package com.aerorescue.auth.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class User {
    private UUID id;
    private String username;
    private String password;
    private Role role;
    private String region;
    private Integer clearanceLevel;
    private List<String> allowedMissionTypes;
    private String organizationId;
    private String mfaSecret;
    private boolean mfaEnabled;
    private boolean active;
}
