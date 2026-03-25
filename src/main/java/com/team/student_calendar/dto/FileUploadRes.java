package com.team.student_calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRes {

    private Long fileId;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private LocalDateTime registeredAt;
}
