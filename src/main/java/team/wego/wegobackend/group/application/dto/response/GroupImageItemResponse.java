package team.wego.wegobackend.group.application.dto.response;

import team.wego.wegobackend.group.domain.entity.GroupImage;

public record GroupImageItemResponse(
        Long id,
        int sortOrder,
        String imageUrl440x240,
        String imageUrl100x100
) {

    public static GroupImageItemResponse from(
            GroupImage entity,
            String imageUrl440x240,
            String imageUrl100x100
    ) {
        return new GroupImageItemResponse(
                entity.getId(),
                entity.getSortOrder(),
                imageUrl440x240,
                imageUrl100x100
        );
    }
}

