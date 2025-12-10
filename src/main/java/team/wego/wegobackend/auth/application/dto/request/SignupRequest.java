package team.wego.wegobackend.auth.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

    @Email(message = "올바른 이메일 형식이 아닙니다")
    @NotBlank(message = "이메일은 필수입니다")
    @Size(max = 50, message = "이메일은 최대 50자까지 가능합니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Pattern(regexp = "^(?=.*[!@#$%^&*]).*$",
        message = "특수문자(!@#$%^&*)를 1개 이상 포함해야 합니다")
    @Size(min = 8, max = 20, message = "비밀번호는 8-20자여야 합니다.")
    private String password;

    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 2, max = 14, message = "닉네임은 2-14자여야 합니다")
    private String nickName;

}
