package team.wego.wegobackend.user.application.dto.response;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.wego.wegobackend.common.security.Role;
import team.wego.wegobackend.user.domain.User;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserInfoResponse {

    private Long userId;

    private String email;

    private Role role;

    private String nickName;

    private String phoneNumber;

    private String mbti;

    private String profileImage;

    private String profileMessage;

    private Boolean isNotificationEnabled;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .role(user.getRole())
            .nickName(user.getNickName())
            .phoneNumber(user.getPhoneNumber())
            .mbti(user.getMbti())
            .profileImage(user.getProfileImage())
            .profileMessage(user.getProfileMessage())
            .isNotificationEnabled(user.getNotificationEnabled())
            .isDeleted(user.getDeleted())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
