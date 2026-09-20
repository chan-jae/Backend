package com.team.student_calendar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableScheduling
@SpringBootApplication
public class StudentCalendarApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		SpringApplication.run(StudentCalendarApplication.class, args);
	}

}
