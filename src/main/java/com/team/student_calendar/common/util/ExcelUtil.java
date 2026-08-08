package com.team.student_calendar.common.util;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExcelUtil {


    /**
     * 액셀파일 Workbook(자바객체로 표현한 액셀파일)으로 변환
     * @param file 파일
     * @return Workbook
     */
    public static Workbook convertToWorkbook(MultipartFile file) {

        // 파일 자체가 있는지 체크
        if (file == null || file.isEmpty()) {
            throw new BaseException(BookErrorCode.INVALID_EXCEL_FILE, "업로드된 액셀 파일이 없습니다.");
        }

        // 액셀파일이 맞는지 체크
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            throw new BaseException(BookErrorCode.INVALID_EXCEL_FILE, "액셀 파일이 아닙니다.");
        }
        String extension = fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();
        if (!("xlsx".equals(extension) || "xls".equals(extension))) {
            throw new BaseException(BookErrorCode.INVALID_EXCEL_FILE, "액셀 파일이 아닙니다.");
        }

        // Workbook으로 변환
        Workbook workbook;
        try (InputStream is = file.getInputStream()) {
            workbook = WorkbookFactory.create(is);
        } catch (Exception e) {
            throw new BaseException(BookErrorCode.INVALID_EXCEL_FILE, "엑셀 파일을 읽을 수 없습니다. 파일이 손상되었거나 암호화되어 있습니다.");
        }

        // 시트 및 행이 실제로 존재하는지 체크 (내용이 빈 액셀)
        try (workbook) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new BaseException(BookErrorCode.INVALID_EXCEL_FILE, "액셀 파일에 데이터가 없습니다.");
            }
        } catch (Exception e) {
            throw new BaseException(BookErrorCode.INVALID_EXCEL_FILE, "파일 처리 중 오류가 발생했습니다.");
        }

        return workbook;
    }


    /**
     * Workbook의 첫 번째 시트를 행 단위로 읽어, 각 행의 0번째 열부터 columnCount번째 열 전까지의 셀 값을 List로 담는다.
     * @param workbook 액셀 Workbook
     * @param columnCount 읽을 열 개수 (0번 열부터 columnCount - 1번 열까지)
     * @return 행마다 셀 값 List를 담은 배열
     */
    public static List<String>[] extractCellData(Workbook workbook, int columnCount) {

        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter dataFormatter = new DataFormatter();

        @SuppressWarnings("unchecked")
        List<String>[] rows = new List[sheet.getPhysicalNumberOfRows()];

        int idx = 0;
        for (Row row : sheet) {
            if (row == null || row.getPhysicalNumberOfCells() == 0) {
                continue;
            }
            List<String> cellValues = new ArrayList<>();
            for (int col = 0; col < columnCount; col++) {
                cellValues.add(dataFormatter.formatCellValue(row.getCell(col)));
            }
            rows[idx++] = cellValues;
        }

        // continue 됬을때 남아있는 null값 다듬고 넘기기
        return idx == rows.length ? rows : Arrays.copyOf(rows, idx);
    }
}
