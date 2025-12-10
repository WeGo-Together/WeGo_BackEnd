package team.wego.wegobackend.group.application.dto.response;

import team.wego.wegobackend.group.domain.entity.GroupImage;

public record GroupImageItemResponse(
        Long imageId440x240,
        Long imageId100x100,
        int sortOrder,
        String imageUrl440x240,
        String imageUrl100x100
) {

    public static GroupImageItemResponse from(
            GroupImage main,   // 440x240
            GroupImage thumb   // 100x100: nullable
    ) {
        Long mainId = (main != null) ? main.getId() : null;
        Long thumbId = (thumb != null) ? thumb.getId() : null;

        int sortOrder = (main != null)
                ? main.getSortOrder()
                : (thumb != null ? thumb.getSortOrder() : 0);

        String mainUrlVal = (main != null) ? main.getImageUrl() : null;
        String thumbUrlVal = (thumb != null) ? thumb.getImageUrl() : null;

        return new GroupImageItemResponse(
                mainId,
                thumbId,
                sortOrder,
                mainUrlVal,
                thumbUrlVal
        );
    }

    public static GroupImageItemResponse fromMainOnly(GroupImage main) {
        return from(main, null);
    }
}


