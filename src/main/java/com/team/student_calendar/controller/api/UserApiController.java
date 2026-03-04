package com.team.student_calendar.controller.api;

import com.team.student_calendar.entity.UserEntity;
import com.team.student_calendar.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Tag(name = "유저", description = "UserApiController")
public class UserApiController {

    private final UserRepository userRepository;

    @PostMapping("/api/test")
    @Operation(summary = "테스트", description = "테스트 API")
    public void test() {

        UserEntity userEntity = UserEntity.builder()
                .username("root")
                .password("0000")
                .nickname("test")
                .build();

        userRepository.save(userEntity);
    }
}
