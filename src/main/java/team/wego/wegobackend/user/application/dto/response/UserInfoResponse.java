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

    private String nickName;

    private String mbti;

    private String profileImage;

    private String profileMessage;

    private int followeesCnt;

    private int followersCnt;

    private int groupJoinedCnt;

    private int groupCreatedCnt;

    private Boolean isNotificationEnabled;

    private LocalDateTime createdAt;

    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .nickName(user.getNickName())
            .mbti(user.getMbti())
            .profileImage(user.getProfileImage())
            .profileMessage(user.getProfileMessage())
            .followeesCnt(user.getFolloweesCnt())
            .followersCnt(user.getFollowersCnt())
            .groupJoinedCnt(user.getGroupJoinedCnt())
            .groupCreatedCnt(user.getGroupCreatedCnt())
            .isNotificationEnabled(user.getNotificationEnabled())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
