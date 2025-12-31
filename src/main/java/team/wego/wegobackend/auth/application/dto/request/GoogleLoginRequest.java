package team.wego.wegobackend.auth.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
    @NotBlank(message = "Authorization code는 필수입니다")
    String authorizationCode,

    @NotBlank(message = "Redirect URI는 필수입니다")
    String redirectUri
) {

}
