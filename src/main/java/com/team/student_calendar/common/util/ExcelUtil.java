package com.team.student_calendar.common.util;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public class ExcelUtil {


    /**
     * 액셀파일 Workbook(자바객체로 표현한 액셀파일)으로 변환
     * @param file 파일
     * @return Workbook
     */
    public static Workbook convertToWorkbook(MultipartFile file) {

        // 파일 자체가 있는지 체크
        if (file == null || file.isEmpty()) {
            throw new BaseException(BookErrorCode.EXCEL_FILE_EMPTY);
        }

        // 액셀파일이 맞는지 체크
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            throw new BaseException(BookErrorCode.NOT_EXCEL_FILE);
        }
        String extension = fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();
        if (!("xlsx".equals(extension) || "xls".equals(extension))) {
            throw new BaseException(BookErrorCode.NOT_EXCEL_FILE);
        }

        // Workbook으로 변환
        Workbook workbook;
        try (InputStream is = file.getInputStream()) {
            workbook = WorkbookFactory.create(is);
        } catch (Exception e) {
            throw new BaseException(BookErrorCode.EXCEL_CANT_READ);
        }

        // 시트 및 행이 실제로 존재하는지 체크 (내용이 빈 액셀)
        try (workbook) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new BaseException(BookErrorCode.EXCEL_NO_DATA);
            }
        } catch (Exception e) {
            throw new BaseException(BookErrorCode.EXCEL_ERROR);
        }

        return workbook;
    }
}
