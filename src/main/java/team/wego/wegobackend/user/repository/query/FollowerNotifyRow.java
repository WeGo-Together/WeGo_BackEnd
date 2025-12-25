package team.wego.wegobackend.user.repository.query;

public record FollowerNotifyRow(
        Long followId,
        Long userId,
        String nickName,
        String profileImage
) {

}

