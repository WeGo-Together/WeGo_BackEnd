package team.wego.wegobackend.user.application.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class FollowResponse {

    private final Long followId;    //cursor
    private final Long userId;
    private final String profileImage;
    private final String nickname;
    private final String profileMessage;

    @QueryProjection
    public FollowResponse(
        Long followId,
        Long userId,
        String profileImage,
        String nickname,
        String profileMessage
    ) {
        this.followId = followId;
        this.userId = userId;
        this.profileImage = profileImage;
        this.nickname = nickname;
        this.profileMessage = profileMessage;
    }
}
