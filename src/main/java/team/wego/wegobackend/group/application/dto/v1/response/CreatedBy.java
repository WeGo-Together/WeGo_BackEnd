package team.wego.wegobackend.group.application.dto.v1.response;

public record CreatedBy(
        Long userId,
        String nickName,
        String profileImage
) {

}
