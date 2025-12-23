package team.wego.wegobackend.group.v2.application.dto.common;

import java.util.List;
import team.wego.wegobackend.group.v2.domain.entity.GroupImageV2;

public record GroupImageItem(
        Long groupImageId,
        String imageKey,
        int sortOrder,
        List<GroupImageVariantItem> variants
) {

    public static GroupImageItem from(GroupImageV2 image) {
        return new GroupImageItem(
                image.getId(),
                image.getImageKey(),
                image.getSortOrder(),
                image.getVariants().stream().map(GroupImageVariantItem::from).toList()
        );
    }

    // DB에 이미지가 0개일 때 내려주는 기본 이미지(440/100)
    public static GroupImageItem defaultLogo() {
        return new GroupImageItem(
                null,
                null,
                0,
                null
        );
    }
}
