package team.wego.wegobackend.user.application.dto.response;

import lombok.Getter;

@Getter
public class WrapperFollowerResponse {

    private final Long followId;    //cursor
    private final Long userId;
    private final String profileImage;
    private final String nickname;
    private final String profileMessage;
    private final boolean isFollow;

    public WrapperFollowerResponse(FollowResponse followResponse, boolean isFollow) {
        this.followId = followResponse.getFollowId();
        this.userId = followResponse.getUserId();
        this.profileImage = followResponse.getProfileImage();
        this.nickname = followResponse.getNickname();
        this.profileMessage = followResponse.getProfileMessage();
        this.isFollow = isFollow;
    }

}
