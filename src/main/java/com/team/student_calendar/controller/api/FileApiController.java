package com.team.student_calendar.controller.api;

import com.team.student_calendar.common.response.ApiSuccessResponse;
import com.team.student_calendar.dto.FileUploadRes;
import com.team.student_calendar.dto.PresignedUrlRes;
import com.team.student_calendar.service.file.UploadFileService;
import com.team.student_calendar.service.file.UrlFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@AllArgsConstructor
@Tag(name = "파일", description = "FileApiController")
public class FileApiController {

    private final UploadFileService uploadFileService;
    private final UrlFileService urlFileService;



    @Operation(summary = "파일 업로드", description = "책(book)별 파일(이미지·PDF 등)을 S3에 업로드하고 메타데이터를 저장합니다.")
    @PostMapping(value = "/api/books/{bookId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiSuccessResponse<FileUploadRes>> uploadBookFile(
            @Parameter(description = "책 ID") @PathVariable("bookId") Long bookId,
            @Parameter(description = "업로드할 파일") @RequestPart("file") MultipartFile file
    ) {
        log.debug("[FileApiController.uploadBookFile] bookId={}, originalFilename={}", bookId, file.getOriginalFilename());

        FileUploadRes res = uploadFileService.uploadFile(bookId, file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.created(res, "파일 업로드에 성공했습니다.", "SUCCESS"));
    }


    @Operation(summary = "파일 Presigned URL 발급", description = "업로드된 파일에 대한 S3 GET Presigned URL을 발급합니다.")
    @GetMapping("/api/books/{bookId}/file-url")
    public ResponseEntity<ApiSuccessResponse<PresignedUrlRes>> getBookFilePresignedUrl(
            @Parameter(description = "책 ID") @PathVariable("bookId") Long bookId
    ) {
        PresignedUrlRes res = urlFileService.getBookFilePresignedUrl(bookId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok(res, "Presigned URL 발급에 성공했습니다.", "SUCCESS"));
    }
}
