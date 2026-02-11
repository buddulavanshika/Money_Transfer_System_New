package com.mts.application.dto;

import java.util.Set;

public record UserProfileResponse(
        Long id,
        String username,
        String fullName,
        String email,
        Set<String> roles) {
}
