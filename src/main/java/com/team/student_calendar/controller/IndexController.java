package com.team.student_calendar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IndexController {

    @GetMapping("/index-hello")
    @ResponseBody
    public String index() {
        return "Hello World!";
    }
}
