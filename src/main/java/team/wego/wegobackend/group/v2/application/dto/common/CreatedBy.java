package team.wego.wegobackend.group.v2.application.dto.common;

import team.wego.wegobackend.user.domain.User;

public record CreatedBy(
        Long userId,
        String nickName,
        String profileImage,
        String profileMessage
) {

    public static CreatedBy from(User host) {
        return new CreatedBy(host.getId(), host.getNickName(), host.getProfileImage(),
                host.getProfileMessage());
    }
}


