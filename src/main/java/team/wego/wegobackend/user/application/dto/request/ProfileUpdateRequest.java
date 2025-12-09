package team.wego.wegobackend.user.application.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequest {

    @Size(min = 2, max = 30, message = "닉네임은 2-30자여야 합니다")
    private String nickName;

    private Boolean notificationEnabled;

    @Size(max = 4, message = "MBTI는 4자여야 합니다")
    private String mbti;

    @Size(max = 50, message = "프로필 메시지는 50자 이하여야 합니다")
    private String profileMessage;
}
