package team.wego.wegobackend.group.application.dto.v1.request;

public record CreateGroupImageRequest(
        Integer sortOrder,
        String imageUrl440x240,
        String imageUrl100x100
) {}

