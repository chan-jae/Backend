package com.team.student_calendar.service.file;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.common.exception.domain.FileErrorCode;
import com.team.student_calendar.config.S3Properties;
import com.team.student_calendar.dto.QuestionSheet;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.entity.FileEntity;
import com.team.student_calendar.repository.FileRepository;
import com.team.student_calendar.service.book.SelectBookService;
import com.team.student_calendar.service.file.util.PdfRenderUtil;
import com.team.student_calendar.service.file.util.UploadFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
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
    private final PdfRenderUtil pdfRenderUtil;
    private final DeleteFileService deleteFileService;


    @CacheEvict(cacheNames = "books", allEntries = true)
    @Transactional
    public void uploadFile(Long bookId, MultipartFile file) {

        log.info("try to upload file by bookId={}", bookId);

        /* 파일 없으면 throw */
        if (file == null || file.isEmpty()) {
            log.warn("no selected file");
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

        /* 메타 데이터 설정*/
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
            log.warn("failed file save");
            throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            log.warn("failed file save to s3 key={}, error={}", s3Key, e.getMessage());
            throw new BaseException(FileErrorCode.FAIL_TO_SAVE_FILE);
        }
        log.info("bucket putObject done key={}", s3Key);

        log.info("completed file save id={}, bookId={}, s3Key={}, originalName={}, sizeBytes={}, contentType={}",
                saved.getId(), bookId, s3Key, originalFilename, saved.getFileSize(), saved.getContentType());

//        return FileUploadRes.builder()
//                .fileId(saved.getId())
//                .originalName(saved.getOriginalName())
//                .contentType(saved.getContentType())
//                .fileSize(saved.getFileSize())
//                .registeredAt(saved.getRegisteredAt())
//                .build();
    }


    /**
     * 마이북 활동지 문제를 문제지 / 정답지 PDF 두 개로 만들어 S3에 저장
     * file 테이블은 책과 1:1(@OneToOne)이라 문제지 키만 저장, 정답지 키는 같은 UUID라 문제지 키에서 구함
     * (mb_pdfs/{bookId}_{uuid} -> mb_pdfs/answer_{bookId}_{uuid})
     * 책에 이미 file이 있으면 그 키에 덮어씀 (ReUploadFileService.reuploadMyBookPdf처럼)
     * uploadFile처럼 DB 먼저 저장하고 S3는 마지막 -> S3 실패 시 예외로 DB 롤백 (호출 쪽 트랜잭션도 같이 롤백)
     * @param bookId 책 id
     * @param sheet Claude가 채운 활동지 문제
     */
    @CacheEvict(cacheNames = "books", allEntries = true)
    @Transactional
    public void uploadMyBookPdf(Long bookId, QuestionSheet sheet) {

        log.info("try to upload mybook pdf by bookId={}", bookId);

        BookEntity book = selectBookService.findById(bookId);

        byte[] questionPdf = pdfRenderUtil.renderQuestionSheet(book, sheet);
        byte[] answerPdf = pdfRenderUtil.renderQuestionSheetAnswer(book, sheet);

        /* 이미 등록된 파일이 있으면 그 키에 덮어쓰기 (S3에 안 쓰는 파일이 쌓이지 않게), 없으면 새 키로 저장 */
        FileEntity existing = fileRepository.findFirstByBook_Id(bookId).orElse(null);
        String questionKey;
        if (existing != null) {
            questionKey = existing.getS3Key();
            existing.setOriginalName(book.getTitle() + " 활동지.pdf");
            existing.setFileSize((long) questionPdf.length);
            existing.setContentType(MediaType.APPLICATION_PDF_VALUE);
        } else {
            questionKey = String.format("mb_pdfs/%d_%s", bookId, UUID.randomUUID());
            /* 메타데이터 db 저장 (문제지 기준) */
            fileRepository.save(FileEntity.builder()
                    .book(book)
                    .s3Key(questionKey)
                    .originalName(book.getTitle() + " 활동지.pdf")
                    .fileSize((long) questionPdf.length)
                    .contentType(MediaType.APPLICATION_PDF_VALUE)
                    .registeredAt(LocalDateTime.now())
                    .build());
        }
        String answerKey = uploadFileUtil.mybookAnswerKeyOf(questionKey);

        putPdf(questionKey, questionPdf);
        try {
            putPdf(answerKey, answerPdf);
        } catch (BaseException e) {
            // 새로 올린 문제지만 남지 않게 (기존 키는 지우면 DB가 없는 파일을 가리키게 되므로 그대로 둠)
            if (existing == null) {
                deleteFileService.deleteQuietly(questionKey);
            }
            throw e;
        }

        log.info("completed mybook pdf save bookId={}, questionKey={}, answerKey={}", bookId, questionKey, answerKey);
    }


    /**
     * PDF를 S3에 저장 (같은 키가 있으면 덮어씀, ReUploadFileService에서도 사용)
     * @param s3Key S3 키
     * @param pdf PDF 바이트
     */
    public void putPdf(String s3Key, byte[] pdf) {

        ObjectMetadata metadata = uploadFileUtil.makeMetaData((long) pdf.length, MediaType.APPLICATION_PDF_VALUE);
        try {
            amazonS3.putObject(s3Properties.getBucket(), s3Key, new ByteArrayInputStream(pdf), metadata);
        } catch (Exception e) {
            log.warn("failed file save to s3 key={}, error={}", s3Key, e.getMessage());
            throw new BaseException(FileErrorCode.FAIL_TO_SAVE_FILE);
        }

        log.info("bucket putObject done key={}", s3Key);
    }
}
