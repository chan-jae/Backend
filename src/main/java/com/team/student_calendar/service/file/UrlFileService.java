package com.team.student_calendar.service.file;

import com.amazonaws.services.s3.AmazonS3;
import com.team.student_calendar.dto.PresignedUrlRes;
import com.team.student_calendar.entity.FileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class UrlFileService {

    private final SelectFileService selectFileService;
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.presigned-url-expiration-seconds}")
    private long presignedUrlExpirationSeconds;

    @Value("${cloud.aws.s3.allowed-content-types}")
    private String allowedContentTypesCsv;


    @Transactional(readOnly = true)
    public PresignedUrlRes getBookFilePresignedUrl(Long bookId) {

        FileEntity fileEntity = selectFileService.findFirstByBookId(bookId);

        long expirationMillis = presignedUrlExpirationSeconds * 1000L;
        Date expiration = new Date(System.currentTimeMillis() + expirationMillis);
        URL url = amazonS3.generatePresignedUrl(bucket, fileEntity.getS3Key(), expiration);

        return PresignedUrlRes.builder()
                .url(url.toExternalForm())
                .expiresAt(Instant.ofEpochMilli(expiration.getTime()))
                .build();
    }
}
