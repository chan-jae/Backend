package com.team.student_calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlRes {

    private String url;
    private String originalName;
    private Instant expiresAt;
}
