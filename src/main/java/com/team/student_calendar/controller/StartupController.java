package com.team.student_calendar.controller;

import com.team.student_calendar.entity.IsbnLookupEntity;
import com.team.student_calendar.repository.IsbnLookupRepository;
import com.team.student_calendar.service.isbnlookup.InsertIsbnLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartupController {

    private final IsbnLookupRepository isbnLookupRepository;
    private final InsertIsbnLookupService insertIsbnLookupService;

    // 서버 시작 시, 활동지 문제는 저장됐는데 PDF(file)가 없는 책을 다시 업로드
    @EventListener(ApplicationReadyEvent.class)
    public void uploadMissingMyBookPdfs() {

        log.info("[startup] start upload missing mybook pdfs");

        List<IsbnLookupEntity> lookups = isbnLookupRepository.findQuestionSheetsWithoutPdf();
        log.info("[startup] missing mybook pdf count={}", lookups.size());

        // 같은 책에 조회 기록이 여러 개면 가장 최근 것만 (최근 것부터 조회)
        Set<Long> doneBookIds = new HashSet<>();
        int success = 0;
        for (IsbnLookupEntity lookup : lookups) {
            Long bookId = lookup.getBook().getId();
            if (!doneBookIds.add(bookId)) {
                continue;
            }
            // 조회 기록 SUCCESS(error 비움) 저장 + PDF 업로드를 한 트랜잭션으로 (업로드 실패 시 상태도 롤백)
            // 한 권 실패해도 나머지는 계속, 서버 시작도 막지 않음
            try {
                insertIsbnLookupService.saveQuestionSheet(lookup.getBook(), lookup, lookup.getQuestionSheet());
                success++;
                log.info("[startup] upload mybook pdf done, status -> SUCCESS lookupId={}, bookId={}", lookup.getId(), bookId);
            } catch (Exception e) {
                log.error("[startup] upload mybook pdf fail lookupId={}, bookId={}, error={}", lookup.getId(), bookId, e.getMessage());
            }
        }

        log.info("[startup] finish upload missing mybook pdfs success={}/{}", success, doneBookIds.size());
    }
}
