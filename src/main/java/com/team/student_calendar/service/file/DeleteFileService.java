package com.team.student_calendar.service.file;

import com.amazonaws.services.s3.AmazonS3;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.FileErrorCode;
import com.team.student_calendar.config.S3Properties;
import com.team.student_calendar.entity.FileEntity;
import com.team.student_calendar.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
