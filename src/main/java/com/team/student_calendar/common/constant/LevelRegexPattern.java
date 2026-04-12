package com.team.student_calendar.common.constant;

import lombok.Getter;

import java.util.regex.Pattern;

@Getter
public enum LevelRegexPattern {

    /**
     * 도서 level (예: A_0, B_12). 괄호가 캡처 그룹 — Matcher {@code group(1)}=티어 글자, {@code group(2)}=숫자.
     */
    LEVEL("^([A-B])_(\\d{1,2})$");

    private final String pattern;
    private final Pattern compiledPattern;

    LevelRegexPattern(String pattern) {
        this.pattern = pattern;
        this.compiledPattern = Pattern.compile(pattern);
    }

    public boolean matches(String input) {
        return input != null && this.compiledPattern.matcher(input).matches();
    }

}
