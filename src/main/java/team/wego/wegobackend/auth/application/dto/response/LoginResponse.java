package team.wego.wegobackend.auth.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.wego.wegobackend.user.application.dto.response.UserInfoResponse;
import team.wego.wegobackend.user.domain.User;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    private String accessToken;

    private String refreshToken;

    private String tokenType;

    private Long expiresIn;

    private LocalDateTime expiresAt;

    private UserInfoResponse user;

    public static LoginResponse of(User user, String accessToken, String refreshToken,
        Long expiresIn) {
        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(expiresIn)
            .expiresAt(LocalDateTime.now().plusSeconds(expiresIn))
            .user(UserInfoResponse.from(user))
            .build();
    }
}
