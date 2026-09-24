package com.team.student_calendar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(

        @NotBlank(message = "이름은 필수 항목입니다.")
        @Size(min = 2, max = 10, message = "이름은 2자 이상 10자 이하여야 합니다.")
        String name,

        @NotBlank(message = "로그인 아이디는 필수 항목입니다.")
        @Size(min = 6, max = 20, message = "아이디는 6자 이상 20자 이하여야 합니다.")
        @Pattern(
                regexp = "^[a-zA-Z0-9]+$",
                message = "아이디는 영문과 숫자만 사용할 수 있습니다."
        )
        String username,

        @NotBlank(message = "비밀번호는 필수 항목입니다.")
        @Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하여야 합니다.")
        @Pattern(
                regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"|,.<>/?])[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"|,.<>/?]+$",
                message = "비밀번호는 영문, 숫자, 특수문자만 사용할 수 있으며 각각 하나 이상 포함해야 합니다."
        )
        String password,

        @NotBlank(message = "토큰은 필수 항목입니다.")
        String token
) {
}
