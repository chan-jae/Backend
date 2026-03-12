package com.team.student_calendar.service.book.progress;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.dto.BookProgressUpdateReq;
import com.team.student_calendar.dto.BookProgressUpdateRes;
import com.team.student_calendar.entity.BookEntity;
import com.team.student_calendar.entity.BookProgressEntity;
import com.team.student_calendar.service.book.SelectBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UpdateBookProgressService {

    private final SelectBookService selectBookService;
    private final SelectBookProgressService selectBookProgressService;


    /**
     * 다음 단계의 책 가져오기 및 업데이트
     * @param req 학생 id
     * @return 다음 단계의 BookEntity
     */
    @Transactional
    public BookProgressUpdateRes nextBookProgress(BookProgressUpdateReq req) {

        /* 유저의 진행도가 있는지 체크 */
        BookProgressEntity existingBookProgress = selectBookProgressService
                .findByStudentId(req.getStudentId());

        /* 다음 BookEntity 가져오기 */
        BookEntity nextBookEntity = null;
        BookProgressUpdateRes nextBook = null;

        // 책 순서 가져오기
        List<BookEntity> bookEntityList = selectBookService
                .findAllByDifficultyAscAndTitleAsc();

        // 책 전체 순회
        for (int i=0; i<bookEntityList.size(); i++) {
            // 같은 책 아니면 계속 탐색
            if (!bookEntityList.get(i).getId().equals(existingBookProgress.getBook().getId())) {
                continue;
            }

            // 다음 책 없는거 같으면 break
            if (i+1 >= bookEntityList.size()) {
                break;
            }

            boolean isLast = false;

            if (i == bookEntityList.size()-2) isLast = true;

            nextBookEntity = bookEntityList.get(i+1);
            nextBook = new BookProgressUpdateRes(nextBookEntity, false, isLast);

            break;
        }

        // 책 못찾았으면 throw
        if (nextBook == null) {
            throw new BaseException(BookErrorCode.NEXT_BOOK_NOT_FOUND);
        }

        /* 진행상황 업데이트 */
        existingBookProgress.setBook(nextBookEntity);

        /* 값 반환*/
        return nextBook;
    }



    /**
     * 이전 단계의 책 가져오기 및 업데이트
     * @param req 학생 id
     * @return 이전 단계의 BookEntity
     */
    @Transactional
    public BookProgressUpdateRes prevBookProgress(BookProgressUpdateReq req) {

        /* 유저의 진행도가 있는지 체크 */
        BookProgressEntity existingBookProgress = selectBookProgressService
                .findByStudentId(req.getStudentId());

        /* 이전 BookEntity 가져오기 */
        BookEntity prevBookEntity = null;
        BookProgressUpdateRes prevBook = null;

        // 책 순서 가져오기
        List<BookEntity> bookEntityList = selectBookService
                .findAllByDifficultyAscAndTitleAsc();

        // 책 전체 순회
        for (int i=bookEntityList.size()-1; i>=0; i--) {
            // 같은 책 아니면 계속 탐색
            if (!bookEntityList.get(i).getId().equals(existingBookProgress.getBook().getId())) {
                continue;
            }

            // 다음 책 없는거 같으면 break
            if (i-1 <= -1) {
                break;
            }

            boolean isFirst = false;

            if (i == 1) isFirst = true;

            prevBookEntity = bookEntityList.get(i-1);
            prevBook = new BookProgressUpdateRes(prevBookEntity, isFirst, false);

            break;
        }

        // 책 못찾았으면 throw
        if (prevBook == null) {
            throw new BaseException(BookErrorCode.PREV_BOOK_NOT_FOUND);
        }

        /* 진행상황 업데이트 */
        existingBookProgress.setBook(prevBookEntity);

        /* 값 반환*/
        return prevBook;
    }
}
