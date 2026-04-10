package com.team.student_calendar.service.file;

import com.amazonaws.services.s3.AmazonS3;
import com.team.student_calendar.config.S3Properties;
import com.team.student_calendar.dto.PresignedUrlRes;
import com.team.student_calendar.entity.FileEntity;
import lombok.RequiredArgsConstructor;
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
    private final S3Properties s3Properties;


    @Transactional(readOnly = true)
    public PresignedUrlRes getBookFilePresignedUrl(Long bookId) {

        if (!selectFileService.existsFileByBookId(bookId)) {
            return null;
        }

        FileEntity fileEntity = selectFileService.findFirstByBookId(bookId);

        long expirationMillis = s3Properties.getPresignedUrlExpirationSeconds() * 1000L;
        Date expiration = new Date(System.currentTimeMillis() + expirationMillis);
        URL url = amazonS3.generatePresignedUrl(s3Properties.getBucket(), fileEntity.getS3Key(), expiration);

        return PresignedUrlRes.builder()
                .url(url.toExternalForm())
                .originalName(fileEntity.getOriginalName())
                .expiresAt(Instant.ofEpochMilli(expiration.getTime()))
                .build();
    }
}
