package team.wego.wegobackend.auth.application.dto.request;

public record GoogleLoginRequest(String authorizationCode, String redirectUri) {

}
