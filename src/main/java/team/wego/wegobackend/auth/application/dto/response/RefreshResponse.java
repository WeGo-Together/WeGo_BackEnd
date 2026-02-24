package team.wego.wegobackend.auth.application.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RefreshResponse {

    private String accessToken;

    @JsonIgnore
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private Long expiresIn;

    private LocalDateTime expiresAt;

    public static RefreshResponse of(String accessToken, String refreshToken, Long expiresIn) {
        return RefreshResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(expiresIn)
            .expiresAt(LocalDateTime.now().plusSeconds(expiresIn))
            .build();
    }
}
