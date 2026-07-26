package com.team.student_calendar.common.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.team.student_calendar.common.exception.BaseException;
import com.team.student_calendar.common.exception.domain.BookErrorCode;
import com.team.student_calendar.common.exception.domain.CommonErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 레벨별 difficulty 최소/최대값.
 */
@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum LevelDifficultyRange {

    A_0(20, 90),
    B_0(100, 150),
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
    A_7(980, 1020),
    B_7(1030, 1080),
    A_8(1090, 1140),
    B_8(1160, 1200),
    A_9(1210, 1250),
    B_9(1260, 1300);

    private final int low;
    private final int high;

    public static LevelDifficultyRange of(String level) {
        try {
            return LevelDifficultyRange.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BaseException(BookErrorCode.INVALID_LEVEL);
        } catch (Exception e) {
            throw new BaseException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public static void validate(String level, int difficulty) {
        LevelDifficultyRange range = of(level);
        if (difficulty < range.low || difficulty > range.high) {
            throw new BaseException(BookErrorCode.DIFFICULTY_OUT_OF_RANGE);
        }
    }


    /**
     * 레벨에 해당하는 전체 난이도 반환
     * @return
     *
     * {
     *   "A_0": {
     *     "low": 2,
     *     "high": 10
     *   },
     *   "A_1": {
     *     "low": 21,
     *     "high": 101
     *   }
     * }
     *
     *
     */
    public static Map<String, LevelDifficultyRange> asMap() {
        return Arrays.stream(values())
                .collect(Collectors.toMap(
                        Enum::name,
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new));
    }
}
