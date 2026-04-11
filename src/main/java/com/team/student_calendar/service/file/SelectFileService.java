package com.team.student_calendar.service.file;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.FileErrorCode;
import com.team.student_calendar.entity.FileEntity;
import com.team.student_calendar.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SelectFileService {

    private final FileRepository fileRepository;


    /**
     * 책에 해당하는 파일 있는지 확인
     * @param bookId 책 pk
     * @return true/false
     */
    @Transactional(readOnly = true)
    public boolean existsFileByBookId(Long bookId) {

        return fileRepository.existsAllByBook_Id(bookId);
    }


    /**
     *
     */
    @Transactional(readOnly = true)
    public FileEntity findFirstByBookId(Long bookId) {

        return fileRepository.findFirstByBook_Id(bookId)
                .orElseThrow(() -> new BaseException(FileErrorCode.FILE_NOT_FOUND));
    }
}
