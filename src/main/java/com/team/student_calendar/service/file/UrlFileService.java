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


    /**
     *
     * @param bookId 책 pk
     * @return Presigned URL
     */
    @Transactional(readOnly = true)
    public PresignedUrlRes getBookFilePresignedUrl(Long bookId) {

        /* 책 있는지 체크*/
        if (!selectFileService.existsFileByBookId(bookId)) {
            return null;
        }

        /* 파일 메타데이터 가져오기*/
        FileEntity fileEntity = selectFileService.findFirstByBookId(bookId);

        /* 만료시간 계산*/
        long expirationMillis = s3Properties.getPresignedUrlExpirationSeconds() * 1000L;
        Date expiration = new Date(System.currentTimeMillis() + expirationMillis);

        /* 만료시간을 가지는 URL 가져오기*/
        URL url = amazonS3.generatePresignedUrl(s3Properties.getBucket(), fileEntity.getS3Key(), expiration);

        return PresignedUrlRes.builder()
                .url(url.toExternalForm())
                .originalName(fileEntity.getOriginalName())
                .expiresAt(Instant.ofEpochMilli(expiration.getTime()))
                .build();
    }
}
