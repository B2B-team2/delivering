package com.sparta.userservice.auth.presentation.dto.request;

import com.sparta.userservice.user.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignupRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "password는 필수입니다.")
    @Size(min = 8, max = 15, message = "password는 8자 이상 15자 이하여야 합니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,15}$",
            message = "비밀번호는 8~15자 영문+숫자+특수문자 포함이어야 합니다."
    )
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    private String phone;

    @NotBlank(message = "SlackID는 필수입니다.")
    private String slackId;

    private String affiliation;

    private Role role;
}