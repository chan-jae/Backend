package com.team.student_calendar.service.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.common.exception.domain.FileErrorCode;
import com.team.student_calendar.config.S3Properties;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.entity.FileEntity;
import com.team.student_calendar.repository.FileRepository;
import com.team.student_calendar.service.book.SelectBookService;
import com.team.student_calendar.service.file.util.UploadFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadFileService {

    private final UploadFileUtil uploadFileUtil;
    private final FileRepository fileRepository;
    private final AmazonS3 amazonS3;
    private final SelectBookService selectBookService;
    private final SelectFileService selectFileService;
    private final S3Properties s3Properties;



    @Transactional
    public void uploadFile(Long bookId, MultipartFile file) {

        log.info("try to upload file by bookId={}", bookId);

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

        log.debug("getOriginalFilename() : {}", file.getOriginalFilename());

        String originalFilename = file.getOriginalFilename();
        String mimeType = uploadFileUtil.checkFileTypeValidation(file);
        Long fileSize = file.getSize();

        log.info("mimeType = {}", mimeType);

        log.info("originalName={}", originalFilename);

        /* S3에 저장할 키 생성 */
        String s3Key = String.format("pdfs/%d_%s", bookId, UUID.randomUUID());

        ObjectMetadata metadata = uploadFileUtil.makeMetaData(fileSize, mimeType);

        /* 메타데이터 db 저장*/
        FileEntity entity = FileEntity.builder()
                .book(book)
                .s3Key(s3Key)
                .originalName(originalFilename)
                .fileSize(file.getSize())
                .contentType(mimeType)
                .registeredAt(LocalDateTime.now())
                .build();
        FileEntity saved = fileRepository.save(entity);

        /* AWS S3 버킷에 저장*/
        try (InputStream in = file.getInputStream()) {
            amazonS3.putObject(s3Properties.getBucket(), s3Key, in, metadata);
        } catch (IOException e) {
            throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        log.info("bucket putObject done key={}", s3Key);

        log.info("completed saved file id={}, bookId={}, s3Key={}, originalName={}, sizeBytes={}, contentType={}",
                saved.getId(), bookId, s3Key, originalFilename, saved.getFileSize(), saved.getContentType());

//        return FileUploadRes.builder()
//                .fileId(saved.getId())
//                .originalName(saved.getOriginalName())
//                .contentType(saved.getContentType())
//                .fileSize(saved.getFileSize())
//                .registeredAt(saved.getRegisteredAt())
//                .build();
    }
}
