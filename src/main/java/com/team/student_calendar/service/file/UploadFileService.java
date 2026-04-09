package com.team.student_calendar.service.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.common.exception.domain.FileErrorCode;
import com.team.student_calendar.dto.FileUploadRes;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.entity.FileEntity;
import com.team.student_calendar.repository.FileRepository;
import com.team.student_calendar.service.book.SelectBookService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadFileService {

    private final Tika tika = new Tika();
    private final FileRepository fileRepository;
    private final AmazonS3 amazonS3;
    private final SelectBookService selectBookService;
    private final SelectFileService selectFileService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.presigned-url-expiration-seconds}")
    private long presignedUrlExpirationSeconds;

    @Value("${cloud.aws.s3.allowed-content-types}")
    private String allowedContentTypesCsv;

    private Set<String> allowedContentTypes;

    @PostConstruct
    void initAllowedContentTypes() {
        allowedContentTypes = Arrays.stream(allowedContentTypesCsv.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toUnmodifiableSet());
    }



    @Transactional
    public FileUploadRes uploadFile(Long bookId, MultipartFile file) {

        log.info("start uploadFile bookId={}", bookId);

        /* 파일 없으면 throw */
        if (file == null || file.isEmpty()) {
            throw new BaseException(FileErrorCode.EMPTY_FILE);
        }

        /* 책 찾기 */
        BookEntity book = selectBookService.findById(bookId);

        /* 이미 pdf 파일이 등록되어 있으면 throw */
        if (selectFileService.existsFileByBookId(book.getId())) {
            throw new BaseException(FileErrorCode.ALREADY_EXISTS_FILE);
        }

        String originalFilename = StringUtils.getFilename(file.getOriginalFilename());
        if (!StringUtils.hasText(originalFilename)) {
            originalFilename = "file";
        }

        String mimeType;
        try {
            mimeType = detectMimeType(file);
        } catch (IOException e) {
            throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        if (!StringUtils.hasText(mimeType) || !allowedContentTypes.contains(mimeType.toLowerCase())) {
            throw new BaseException(FileErrorCode.UNSUPPORTED_FILE_TYPE);
        }
        /* MIME 타입*/
        String mime = mimeType.toLowerCase();
        /* 확장자*/
        String extension = getFileExtension(originalFilename);
        log.info("originalName={}, mime={}, extension={}", originalFilename, mime, extension);

        /* S3에 저장할 키 생성 */
        String s3Key = String.format("pdfs/%d_%s%s", bookId, UUID.randomUUID(), extension);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(mime);

        /* 메타데이터 db 저장*/
        FileEntity entity = FileEntity.builder()
                .book(book)
                .s3Key(s3Key)
                .originalName(originalFilename)
                .fileSize(file.getSize())
                .contentType(mime)
                .registeredAt(LocalDateTime.now())
                .build();
        FileEntity saved = fileRepository.save(entity);

        /* AWS S3 버킷에 저장*/
        try (InputStream in = file.getInputStream()) {
            amazonS3.putObject(bucket, s3Key, in, metadata);
        } catch (IOException e) {
            throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        log.info("bucket putObject done key={}", s3Key);

        log.info("completed saved file entity id={}, bookId={}, s3Key={}, originalName={}, sizeBytes={}, contentType={}",
                saved.getId(), bookId, s3Key, originalFilename, saved.getFileSize(), saved.getContentType());

        return FileUploadRes.builder()
                .fileId(saved.getId())
                .originalName(saved.getOriginalName())
                .contentType(saved.getContentType())
                .fileSize(saved.getFileSize())
                .registeredAt(saved.getRegisteredAt())
                .build();
    }


    /**
     * Tika로 MIME 타입 판별<br/>
     * Tika는 파일 시작 부분의 byte magic pattern을 읽어서 MIME 타입을 확인한다
     * */
    private String detectMimeType(MultipartFile file) throws IOException {
        String name = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "";
        try (InputStream in = file.getInputStream()) {
            return tika.detect(in, name);
        }
    }


    /** . 뒤에 있는 파일 확장자 가져오기 */
    private static String getFileExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "";
        }
        String base = StringUtils.getFilename(originalFilename);
        if (!StringUtils.hasText(base)) {
            return "";
        }
        int dot = base.lastIndexOf('.');
        if (dot < 0 || dot == base.length() - 1) {
            return "";
        }
        return base.substring(dot).toLowerCase();
    }
}
