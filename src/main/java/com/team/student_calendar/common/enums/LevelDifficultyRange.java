package com.team.student_calendar.common.enums;

import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.CustomBookErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 레벨별 실제 티칭오션 책 difficulty 최소/최대값.
 */
@Getter
@AllArgsConstructor
public enum LevelDifficultyRange {

    A_0(0, 90),
    B_0(100, 170),
    A_1(180, 260),
    B_1(270, 330),
    A_2(350, 410),
    B_2(420, 480),
    A_3(480, 550),
    B_3(560, 610),
    A_4(620, 670),
    B_4(650, 720),
    A_5(730, 790),
    B_5(800, 850),
    A_6(860, 910),
    B_6(920, 960),
    A_7(970, 1020),
    B_7(1030, 1080),
    A_8(1090, 1140),
    B_8(1150, 1200),
    A_9(1210, 1250),
    B_9(1260, 1300);

    private final int low;
    private final int high;

    public static LevelDifficultyRange of(String level) {
        try {
            return LevelDifficultyRange.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BaseException(CustomBookErrorCode.INVALID_LEVEL);
        }
    }

    public void validateDifficulty(int difficulty) {
        if (difficulty < low || difficulty > high) {
            throw new BaseException(CustomBookErrorCode.DIFFICULTY_OUT_OF_RANGE);
        }
    }
}
