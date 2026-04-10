package com.team.student_calendar.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ConfigurationProperties(prefix = "cloud.aws.s3")
@Component
@Getter
@Setter
public class S3Properties {

    private String accessKey;
    private String secretKey;
    private String region;
    private String bucket;
    private long presignedUrlExpirationSeconds;
    private List<String> allowedContentTypes = new ArrayList<>();

    private Set<String> allowedContentTypesSet;

    @PostConstruct
    void initAllowedContentTypesSet() {
        allowedContentTypesSet = allowedContentTypes.stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toUnmodifiableSet());
    }
}
