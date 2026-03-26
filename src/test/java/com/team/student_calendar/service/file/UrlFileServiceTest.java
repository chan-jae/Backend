package com.team.student_calendar.service.file;

import com.amazonaws.services.s3.AmazonS3;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.FileErrorCode;
import com.team.student_calendar.dto.PresignedUrlRes;
import com.team.student_calendar.entity.FileEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlFileServiceTest {

    private static final String BUCKET = "test-bucket";
    private static final long PRESIGNED_SECONDS = 900L;

    @Mock
    private SelectFileService selectFileService;

    @Mock
    private AmazonS3 amazonS3;

    private UrlFileService urlFileService;

    @BeforeEach
    void setUp() {
        urlFileService = new UrlFileService(selectFileService, amazonS3);
        ReflectionTestUtils.setField(urlFileService, "bucket", BUCKET);
        ReflectionTestUtils.setField(urlFileService, "presignedUrlExpirationSeconds", PRESIGNED_SECONDS);
        ReflectionTestUtils.setField(urlFileService, "allowedContentTypesCsv", "application/pdf");
    }



    @Test
    @DisplayName("파일이 없으면 throw FILE_NOT_FOUND")
    void getBookFilePresignedUrl_notFound_throws() {

        // given
        long bookId = 99L;
        /* 책 찾을려 하면 에러 던지기*/
        when(selectFileService.findFirstByBookId(bookId))
                .thenThrow(new BaseException(FileErrorCode.FILE_NOT_FOUND));

        // when
        /* getBookFilePresignedUrl() 실행 시 에러가 발생한다고 가정*/
        BaseException ex = assertThrows(BaseException.class, () -> urlFileService.getBookFilePresignedUrl(bookId));

        // then
        /* 발생한 에러가 FILE_NOT_FOUND 맞는지 체크*/
        assertThat(ex.getErrorCode()).isEqualTo(FileErrorCode.FILE_NOT_FOUND);
        /* Presigned Url 생성 메서드 실행 안됐는지 체크*/
        verify(amazonS3, never()).generatePresignedUrl(any(String.class), any(String.class), any(Date.class));
    }


    @Test
    @DisplayName("책에 파일이 있으면 presigned URL 과 만료 시각을 반환한다")
    void getBookFilePresignedUrl_success() throws Exception {

        // given
        long bookId = 1L;
        String s3Key = "pdfs/1_uuid.pdf";
        FileEntity file = FileEntity.builder()
                .id(10L)
                .s3Key(s3Key)
                .originalName("doc.pdf")
                .fileSize(100L)
                .contentType("application/pdf")
                .registeredAt(LocalDateTime.now())
                .build();
        /* 파일 반환 설정*/
        when(selectFileService.findFirstByBookId(bookId)).thenReturn(file);

        URL presigned = new URL("https://s3.example.com/bucket/object?X-Amz-Signature=test");
        /* Url 반환 설정*/
        when(amazonS3.generatePresignedUrl(eq(BUCKET), eq(s3Key), any(Date.class))).thenReturn(presigned);

        long beforeMillis = System.currentTimeMillis();

        // when
        PresignedUrlRes res = urlFileService.getBookFilePresignedUrl(bookId);

        long afterMillis = System.currentTimeMillis();

        // then
        /* 반환값에 설정된 url이 설정값과 같은지 체크*/
        assertThat(res.getUrl()).isEqualTo(presigned.toExternalForm());
        long expiryMin = beforeMillis + PRESIGNED_SECONDS * 1000;
        long expiryMax = afterMillis + PRESIGNED_SECONDS * 1000;
        /* 반환값에 설정된 만료시간이 함수 실행 전, 후 시간사이에 있는지 체크*/
        assertThat(res.getExpiresAt().toEpochMilli()).isBetween(expiryMin, expiryMax);

        /* 인자로 넘어간 만료일이 함수 실행 전, 후 시간사이에 있는지 체크*/
        ArgumentCaptor<Date> expirationCaptor = ArgumentCaptor.forClass(Date.class);
        verify(amazonS3).generatePresignedUrl(eq(BUCKET), eq(s3Key), expirationCaptor.capture());
        Instant capturedExpiry = Instant.ofEpochMilli(expirationCaptor.getValue().getTime());
        assertThat(capturedExpiry.toEpochMilli()).isBetween(expiryMin, expiryMax);
    }
}
