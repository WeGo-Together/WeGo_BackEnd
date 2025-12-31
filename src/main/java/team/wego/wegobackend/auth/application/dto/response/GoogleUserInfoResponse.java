package team.wego.wegobackend.auth.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class GoogleUserInfoResponse {
    @JsonProperty("sub")
    private String id;
    private String email;
    private String name;
    private String picture;
    @JsonProperty("email_verified")
    private Boolean verifiedEmail;  // 이메일 인증 여부

}
