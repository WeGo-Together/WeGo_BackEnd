package team.wego.wegobackend.group.application.dto.response;

public record CreatedBy(
        Long userId,
        String nickName,
        String profileImage
) {

}
