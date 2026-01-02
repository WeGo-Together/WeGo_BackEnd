package team.wego.wegobackend.notification.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSummary {

    private final Long id;
    private final String nickname;

}
