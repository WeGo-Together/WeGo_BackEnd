package team.wego.wegobackend.group.application.dto.request;

public record CreateGroupImageRequest(
        Integer sortOrder,
        String imageUrl440x240,
        String imageUrl100x100
) {}

