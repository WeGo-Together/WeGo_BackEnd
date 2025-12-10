package team.wego.wegobackend.user.application.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.wego.wegobackend.user.application.dto.Mbti;

@Getter
@NoArgsConstructor
public class ProfileUpdateRequest {

    @Size(min = 2, max = 14, message = "닉네임은 2-14자여야 합니다")
    private String nickName;

    private Mbti mbti;

    @Size(max = 20, message = "프로필 메시지는 20자 이하여야 합니다")
    private String profileMessage;
}
