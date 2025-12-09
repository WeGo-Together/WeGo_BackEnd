package team.wego.wegobackend.group.application.dto.response;


import java.util.List;

public record CreateGroupImageResponse(
        Long groupId,
        List<GroupImageItemResponse> images
) {
}

