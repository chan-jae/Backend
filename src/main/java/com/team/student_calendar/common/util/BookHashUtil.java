package com.team.student_calendar.common.util;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class BookHashUtil {

    public static String generateBookHashKey(String title, String author) {

        String normalized = normalize(title) + normalize(author);
        return sha256Hex(normalized);
    }






    private static String normalize(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replaceAll("\\s+", "") // 모든 공백 제거
                .toUpperCase(Locale.ROOT);             // 알파벳 무조건 대문자
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR, "SHA-256 알고리즘을 찾을 수 없습니다.");
        } catch (Exception e) {
            throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
