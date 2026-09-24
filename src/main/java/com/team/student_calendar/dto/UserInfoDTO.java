package com.team.student_calendar.dto;

import com.team.student_calendar.common.enums.UserRole;

public record UserInfoDTO(
        String username,
        String name,
        UserRole role
) {
}
