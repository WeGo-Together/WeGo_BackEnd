package team.wego.wegobackend.auth.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.wego.wegobackend.user.domain.User;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignupResponse {

    private Long userId;

    private String email;

    private String nickName;

    private String phoneNumber;

    private LocalDateTime createdAt;

    public static SignupResponse from(User user) {
        return SignupResponse.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .nickName(user.getNickName())
            .phoneNumber(user.getPhoneNumber())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
