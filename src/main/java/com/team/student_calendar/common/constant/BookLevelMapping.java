package com.team.student_calendar.common.constant;

import java.util.regex.Matcher;


public final class BookLevelMapping {


    private BookLevelMapping() {
    }


    /**
     * {@code A_0} 레벨을 1로 두고 문자열을 받아서 동적으로 커스텀 난이도 계산.
     * <p>
     * 규칙: {@code difficulty = tierIndex + index * TIER_COUNT + 1}<br/>
     * ({@code tierIndex}: A=0, B=1, … / {@code index}: 밑줄 뒤 숫자)
     * </p>
     */
    public static byte customLevelOf(String level) {

        if (level == null || level.isBlank()) {
            throw new IllegalArgumentException("level is blank");
        }

        level = level.toUpperCase();

        Matcher m = LevelRegexPattern.LEVEL.getCompiledPattern().matcher(level.trim());
        if (!m.matches()) {
            throw new IllegalArgumentException("level must be like A_0, B_3: " + level);
        }

        int tierIndex = m.group(1).charAt(0) - 'A';
        int index = Integer.parseInt(m.group(2));
        return (byte) (tierIndex + index * 2 + 1);
    }
}
