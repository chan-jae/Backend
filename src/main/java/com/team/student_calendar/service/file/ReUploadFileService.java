package com.team.student_calendar.service.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import com.team.student_calendar.common.exception.domain.FileErrorCode;
import com.team.student_calendar.config.S3Properties;
import com.team.student_calendar.dto.QuestionSheet;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.entity.FileEntity;
import com.team.student_calendar.service.book.SelectBookService;
import com.team.student_calendar.service.file.util.PdfRenderUtil;
import com.team.student_calendar.service.file.util.UploadFileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReUploadFileService {

    private final UploadFileUtil uploadFileUtil;
    private final SelectFileService selectFileService;
    private final SelectBookService selectBookService;
    private final AmazonS3 amazonS3;
    private final S3Properties s3Properties;
    private final PdfRenderUtil pdfRenderUtil;
    private final UploadFileService uploadFileService;


    @CacheEvict(cacheNames = "books", allEntries = true)
    @Transactional
    public void reuploadFile(Long bookId, MultipartFile file) {

        log.info("try to re upload file by bookId={}", bookId);

        /* 파일 없으면 throw */
        if (file == null || file.isEmpty()) {
            throw new BaseException(FileErrorCode.EMPTY_FILE);
        }

        /* 책 없으면 throw*/
        BookEntity book = selectBookService.findById(bookId);

        /* 등록되어 있는 파일이 없으면 throw */
        if (!selectFileService.existsFileByBookId(book.getId())) {
            throw new BaseException(FileErrorCode.NO_EXISTING_FILE);
        }

        /* 파일 원본 이름 가져오기*/
        String originalFilename = file.getOriginalFilename();
        /* 파일 mime 체크*/
        String mimeType = uploadFileUtil.checkFileTypeValidation(file);
        /* 파일 크기 */
        Long fileSize = file.getSize();
        log.info("originalName={}", originalFilename);

        /* 파일 메타데이터 가져와서 수정하기*/
        FileEntity fileEntity = selectFileService.findFirstByBookId(bookId);
        fileEntity.setOriginalName(originalFilename);
        fileEntity.setFileSize(fileSize);
        fileEntity.setContentType(mimeType);

        /* 기존에 존재하는 덮어쓸 키 가져오기*/
        String s3Key = fileEntity.getS3Key();
        /* 메타데이터 가져오기*/
        ObjectMetadata metadata = uploadFileUtil.makeMetaData(fileSize, mimeType);

        /* AWS S3 버킷에 덮어쓰기*/
        try (InputStream in = file.getInputStream()) {
            amazonS3.putObject(s3Properties.getBucket(), s3Key, in, metadata);
        } catch (IOException e) {
            throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        log.info("bucket putObject update key={}", s3Key);

        log.info("completed updated file id={}, bookId={}, s3Key={}, originalName={}, sizeBytes={}, contentType={}",
                fileEntity.getId(), bookId, s3Key, originalFilename, fileEntity.getFileSize(), fileEntity.getContentType());
    }


    /**
     * 마이북 활동지 문제지 / 정답지 PDF를 다시 만들어 기존 S3 키에 덮어쓰기 (책 정보 수정 시)
     * 정답지 키는 문제지 키에서 구함 (UploadFileService.uploadMyBookPdf 참고)
     * @param book 수정된 책 (표지에 들어갈 정보)
     * @param sheet 책 정보를 갱신한 활동지 문제
     */
    @Transactional
    public void reuploadMyBookPdf(BookEntity book, QuestionSheet sheet) {

        log.info("try to re upload mybook pdf by bookId={}", book.getId());

        /* 등록되어 있는 파일이 없으면 throw */
        FileEntity fileEntity = selectFileService.findFirstByBookId(book.getId());

        String questionKey = fileEntity.getS3Key();
        String answerKey = uploadFileUtil.mybookAnswerKeyOf(questionKey);

        byte[] questionPdf = pdfRenderUtil.renderQuestionSheet(book, sheet);
        byte[] answerPdf = pdfRenderUtil.renderQuestionSheetAnswer(book, sheet);

        /* 파일 메타데이터 수정 (제목이 바뀌었을 수 있음) */
        fileEntity.setOriginalName(book.getTitle() + " 활동지.pdf");
        fileEntity.setFileSize((long) questionPdf.length);

        /* 기존 키에 덮어쓰기 */
        uploadFileService.putPdf(questionKey, questionPdf);
        uploadFileService.putPdf(answerKey, answerPdf);

        log.info("completed mybook pdf re upload bookId={}, questionKey={}, answerKey={}", book.getId(), questionKey, answerKey);
    }
}
