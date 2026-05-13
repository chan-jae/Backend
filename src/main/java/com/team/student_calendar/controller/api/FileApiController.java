package com.team.student_calendar.controller.api;

import com.team.student_calendar.common.response.ApiSuccessResponse;
import com.team.student_calendar.dto.FileUploadRes;
import com.team.student_calendar.dto.PresignedUrlRes;
import com.team.student_calendar.service.file.DeleteFileService;
import com.team.student_calendar.service.file.ReUploadFileService;
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
    private final DeleteFileService deleteFileService;
    private final ReUploadFileService reUploadFileService;



    @Operation(summary = "파일 업로드", description = "책 pdf를 S3에 업로드하고 메타데이터를 저장")
    @PostMapping(value = "/api/books/{bookId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiSuccessResponse<Void>> uploadBookFile(
            @Parameter(description = "책 ID") @PathVariable("bookId") Long bookId,
            @Parameter(description = "업로드할 파일") @RequestPart("file") MultipartFile file
    ) {

        uploadFileService.uploadFile(bookId, file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.created("파일 업로드에 성공했습니다.", "SUCCESS"));
    }


    @Operation(summary = "파일 Presigned URL 발급", description = "S3 Presigned URL을 발급")
    @GetMapping("/api/books/{bookId}/file-url")
    public ResponseEntity<ApiSuccessResponse<PresignedUrlRes>> getBookFilePresignedUrl(
            @Parameter(description = "책 ID") @PathVariable("bookId") Long bookId
    ) {

        PresignedUrlRes res = urlFileService.getBookFilePresignedUrl(bookId);

        if (res == null) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiSuccessResponse.ok(null, "파일이 존재하지 않습니다.", "FAIL"));
        }
        else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiSuccessResponse.ok(res, "URL 발급에 성공했습니다.", "SUCCESS"));
        }
    }


    @Operation(summary = "파일 삭제 기능", description = "S3 파일 및 메타데이터 삭제")
    @DeleteMapping("/api/books/{bookId}")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteBookFile(
            @PathVariable Long bookId
    ) {

        deleteFileService.deleteFileById(bookId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.ok("파일 삭제에 성공했습니다.", "SUCCESS"));
    }


    @Operation(summary = "파일 변경 기능", description = "s3 key는 그대로 놔두고 파일만 변경")
    @PatchMapping("/api/books/{bookId}/files")
    public ResponseEntity<ApiSuccessResponse<Void>> patchBookFile(
            @PathVariable @Parameter(description = "책 ID") Long bookId,
            @Parameter(description = "업로드할 파일") @RequestPart("file") MultipartFile file
    ) {

        reUploadFileService.reuploadFile(bookId, file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.created("파일 재업로드에 성공했습니다.", "SUCCESS"));
    }
}
