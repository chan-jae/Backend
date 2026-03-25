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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link UploadFileService} 단위 테스트 (Mockito).
 */
@ExtendWith(MockitoExtension.class)
class UploadFileServiceTest {

    private static final String BUCKET = "test-bucket";
    private static final String ALLOWED_TYPES = "application/pdf,image/jpeg,image/png";

    /**
     * Tika가 application/pdf 로 판별할 수 있는 최소 PDF 시그니처.
     */
    private static final byte[] PDF_BYTES = (
            "%PDF-1.4\n%\u00e0\u00e1\u00e2\u00e3\n1 0 obj\n<<>>\nendobj\ntrailer\n<<>>\n%%EOF"
    ).getBytes(StandardCharsets.ISO_8859_1);

    @Mock
    private FileRepository fileRepository;

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private SelectBookService selectBookService;

    @Mock
    private SelectFileService selectFileService;

    private UploadFileService uploadFileService;

    @BeforeEach
    void setUp() {
        uploadFileService = new UploadFileService(fileRepository, amazonS3, selectBookService, selectFileService);
        ReflectionTestUtils.setField(uploadFileService, "bucket", BUCKET);
        ReflectionTestUtils.setField(uploadFileService, "presignedUrlExpirationSeconds", 900L);
        ReflectionTestUtils.setField(uploadFileService, "allowedContentTypesCsv", ALLOWED_TYPES);
        uploadFileService.initAllowedContentTypes();
    }



    @Test
    @DisplayName("파일이 null 이거나 비어 있으면 throw EMPTY_FILE")
    void uploadFile_nullOrEmpty_throws() {

        // given : null 파일 생성
        MockMultipartFile empty = new MockMultipartFile("file", "x.pdf", "application/pdf", new byte[0]);

        // when : 파일이 null인 경우 업로드 시도
        BaseException nullEx = assertThrows(BaseException.class, () -> uploadFileService.uploadFile(1L, null));
        // then : EMPTY_FILE 예외가 발생하는지 체크
        assertThat(nullEx.getErrorCode()).isEqualTo(FileErrorCode.EMPTY_FILE);

        // when : 빈 파일 생성
        BaseException emptyEx = assertThrows(BaseException.class, () -> uploadFileService.uploadFile(1L, empty));
        // then : EMPTY_FILE 예외가 발생하는지 체크
        assertThat(emptyEx.getErrorCode()).isEqualTo(FileErrorCode.EMPTY_FILE);

        /* save() 메서드가 실행 안된게 맞는지 체크*/
        verify(fileRepository, never()).save(any());
        /* S3 삽입 요청 실행 안된게 맞는지 체크*/
        verify(amazonS3, never()).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
    }


    @Test
    @DisplayName("이미 책에 파일이 있으면 throw ALREADY_EXISTS_FILE")
    void uploadFile_alreadyExists_throws() {

        // given : 책 생성
        BookEntity book = sampleBook(1L);
        when(selectBookService.findById(1L)).thenReturn(book);
        when(selectFileService.existsFileByBookId(1L)).thenReturn(true);
        MockMultipartFile multipart = new MockMultipartFile("file", "a.pdf", "application/pdf", PDF_BYTES);

        // when : 파일이 존재하는 상태로 파일 업로드 시도
        BaseException ex = assertThrows(BaseException.class, () -> uploadFileService.uploadFile(1L, multipart));

        // then : ALREADY_EXISTS_FILE 예외가 발생하는지 체크
        assertThat(ex.getErrorCode()).isEqualTo(FileErrorCode.ALREADY_EXISTS_FILE);

        /* save() 메서드가 실행 안된게 맞는지 체크*/
        verify(fileRepository, never()).save(any());
        /* S3 삽입 요청 실행 안된게 맞는지 체크*/
        verify(amazonS3, never()).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
    }


    @Test
    @DisplayName("MIME 판별 시 InputStream IOException 이면 throw INTERNAL_SERVER_ERROR")
    void uploadFile_detectMimeIOException_throws() throws IOException {

        // given : 책 생성
        long bookId = 1L;
        BookEntity book = sampleBook(bookId);
        when(selectBookService.findById(bookId)).thenReturn(book);
        when(selectFileService.existsFileByBookId(bookId)).thenReturn(false);
        MultipartFile broken = Mockito.mock(MultipartFile.class);
        when(broken.isEmpty()).thenReturn(false);
        when(broken.getOriginalFilename()).thenReturn("a.pdf");
        /* getInputStream() 호출시 에러가 발생하도록 설정*/
        when(broken.getInputStream()).thenThrow(new IOException("stream error"));

        // when : MIME 판별하기 위해 getInputStream() 실행 시 에러 발생
        BaseException ex = assertThrows(BaseException.class, () -> uploadFileService.uploadFile(1L, broken));

        // then : IOException 발생 시 INTERNAL_SERVER_ERROR 에러인지 체크
        assertThat(ex.getErrorCode()).isEqualTo(CommonErrorCode.INTERNAL_SERVER_ERROR);

        /* save() 메서드가 실행 안된게 맞는지 체크*/
        verify(fileRepository, never()).save(any());
        /* S3 삽입 요청 실행 안된게 맞는지 체크*/
        verify(amazonS3, never()).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
    }


    @Test
    @DisplayName("허용되지 않은 MIME 이면 throw UNSUPPORTED_FILE_TYPE")
    void uploadFile_unsupportedMime_throws() {

        // given : 책 생성
        BookEntity book = sampleBook(1L);
        when(selectBookService.findById(1L)).thenReturn(book);
        when(selectFileService.existsFileByBookId(1L)).thenReturn(false);
        byte[] plain = "plain text only".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile multipart = new MockMultipartFile(
                "file", "note.txt", "text/plain", plain
        );

        // when : 허용되지 않는 mime 타입 파일로 업로드 시도
        BaseException ex = assertThrows(BaseException.class, () -> uploadFileService.uploadFile(1L, multipart));

        // then : UNSUPPORTED_FILE_TYPE 예외가 발생하는지 체크
        assertThat(ex.getErrorCode()).isEqualTo(FileErrorCode.UNSUPPORTED_FILE_TYPE);

        verify(fileRepository, never()).save(any());
        /* save() 메서드가 실행 안된게 맞는지 체크*/
        verify(fileRepository, never()).save(any());
        /* S3 삽입 요청 실행 안된게 맞는지 체크*/
        verify(amazonS3, never()).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
    }


    @Test
    @DisplayName("S3 업로드 직전 스트림 IOException 이면 INTERNAL_SERVER_ERROR (save 이후)")
    void uploadFile_putObjectStreamIOException_throws() throws IOException {

        // given : 책 생성
        long bookId = 1L;
        BookEntity book = sampleBook(bookId);
        when(selectBookService.findById(bookId)).thenReturn(book);
        when(selectFileService.existsFileByBookId(bookId)).thenReturn(false);
        when(fileRepository.save(any(FileEntity.class))).thenAnswer(invocation -> {
            FileEntity e = invocation.getArgument(0);
            e.setId(50L);
            return e;
        });
        MultipartFile multipart = Mockito.mock(MultipartFile.class);
        when(multipart.isEmpty()).thenReturn(false);
        when(multipart.getOriginalFilename()).thenReturn("sample.pdf");
        when(multipart.getSize()).thenReturn((long) PDF_BYTES.length);
        InputStream first = new ByteArrayInputStream(PDF_BYTES);
        when(multipart.getInputStream())
                .thenReturn(first)
                .thenThrow(new IOException("second open fails"));

        // when : S3에 저장하기 위해 getInputStream() 실행 시 에러 발생
        BaseException ex = assertThrows(BaseException.class, () -> uploadFileService.uploadFile(bookId, multipart));

        // then : IOException 발생 시 INTERNAL_SERVER_ERROR 에러인지 확인
        assertThat(ex.getErrorCode()).isEqualTo(CommonErrorCode.INTERNAL_SERVER_ERROR);

        /* save()가 1번 호출되었어야 함*/
        verify(fileRepository, times(1)).save(any(FileEntity.class));
        /* S3 삽입 요청 실행 안된게 맞는지 체크*/
        verify(amazonS3, never()).putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
    }


    @Test
    @DisplayName("S3에 업로드 성공 후 FileUploadRes 반환")
    void uploadFile_success() {

        // given : 책 생성
        long bookId = 1L;
        BookEntity book = sampleBook(bookId);
        when(selectBookService.findById(bookId)).thenReturn(book);
        when(selectFileService.existsFileByBookId(bookId)).thenReturn(false);
        when(fileRepository.save(any(FileEntity.class))).thenAnswer(invocation -> {
            FileEntity e = invocation.getArgument(0);
            e.setId(100L);
            return e;
        });
        MockMultipartFile multipart = new MockMultipartFile(
                "file", "sample.pdf", "application/pdf", PDF_BYTES
        );

        // when : 정상적으로 업로드 수행
        FileUploadRes res = uploadFileService.uploadFile(bookId, multipart);

        // then
        /* 같은 값을 응답하는지 체크*/
        assertThat(res.getFileId()).isEqualTo(100L);
        assertThat(res.getOriginalName()).isEqualTo("sample.pdf");
        assertThat(res.getContentType()).isEqualTo("application/pdf");
        assertThat(res.getFileSize()).isEqualTo(PDF_BYTES.length);
        assertThat(res.getRegisteredAt()).isNotNull();

        /* S3 저장 요청이 1번이 맞는지 체크*/
        verify(amazonS3, times(1)).putObject(eq(BUCKET), anyString(), any(InputStream.class), any(ObjectMetadata.class));

        /* db에 저장할 FileEntity가 기존에 설정한 값이 맞는지 체크*/
        ArgumentCaptor<FileEntity> entityCaptor = ArgumentCaptor.forClass(FileEntity.class);
        verify(fileRepository, times(1)).save(entityCaptor.capture());
        FileEntity saved = entityCaptor.getValue();
        assertThat(saved.getBook()).isSameAs(book);
        assertThat(saved.getS3Key()).startsWith("pdfs/" + bookId + "_");
        assertThat(saved.getOriginalName()).isEqualTo("sample.pdf");
    }









    private static BookEntity sampleBook(long id) {
        return BookEntity.builder()
                .id(id)
                .title("t")
                .author("a")
                .publisher("p")
                .category("c")
                .level("l")
                .difficulty(1)
                .bookNo(1L)
                .imageUrl("http://img")
                .type((byte) 0)
                .build();
    }
}
