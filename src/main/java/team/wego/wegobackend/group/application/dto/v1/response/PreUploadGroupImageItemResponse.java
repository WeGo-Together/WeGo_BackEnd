package team.wego.wegobackend.group.application.dto.v1.response;

public record PreUploadGroupImageItemResponse(
        Integer sortOrder,
        String imageUrl440x240,
        String imageUrl100x100
) {}