package com.team.student_calendar.common.constant;

import lombok.Getter;

import java.util.regex.Pattern;

@Getter
public enum RegexPattern {

    LEVEL("^[A-B]_\\d{1,2}");

    private final String pattern;
    private final Pattern compiledPattern;


    RegexPattern(String pattern) {
        this.pattern = pattern;
        this.compiledPattern = Pattern.compile(pattern);
    }

    public boolean matches(String input) {
        return input != null && this.compiledPattern.matcher(input).matches();
    }

}
