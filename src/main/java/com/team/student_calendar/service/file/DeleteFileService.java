package com.team.student_calendar.service.file;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.FileErrorCode;
import com.team.student_calendar.config.S3Properties;
import com.team.student_calendar.entity.FileEntity;
import com.team.student_calendar.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class DeleteFileService {

    private final SelectFileService selectFileService;
    private final FileRepository fileRepository;
    private final AmazonS3 amazonS3;
    private final S3Properties s3Properties;


    @CacheEvict(cacheNames = "books", allEntries = true)
    @Transactional
    public void deleteFileById(Long id) {

        log.info("try to delete s3 file");

        FileEntity file = selectFileService.findFirstByBookId(id);
        String s3Key = file.getS3Key();

        /* s3 메타 데이터 삭제 시도*/
        fileRepository.delete(file);

        /* s3 오브젝트 삭제 시도*/
        try {
            amazonS3.deleteObject(s3Properties.getBucket(), s3Key);
        } catch (Exception e) {
            log.warn("fail to delete file in s3");
            throw new BaseException(FileErrorCode.FAIL_TO_DELETE_FILE);
        }

        log.info("complete deleted s3 file");
    }


    /**
     * S3 오브젝트 삭제, 실패해도 예외 없이 로그만 남김 (남은 키는 로그 보고 수동 삭제)
     * 되돌리기 중 삭제까지 실패하면 원래 에러가 가려지지 않게 하려는 용도
     * @param s3Key 삭제할 S3 키
     */
    public void deleteQuietly(String s3Key) {
        try {
            amazonS3.deleteObject(s3Properties.getBucket(), s3Key);
            log.info("bucket deleteObject done key={}", s3Key);
        } catch (SdkClientException e) {
            log.error("orphan s3 object key={}", s3Key, e);
        }
    }
}
