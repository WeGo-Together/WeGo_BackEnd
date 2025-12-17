package team.wego.wegobackend.group.v2.application.dto.common;

import java.util.List;
import team.wego.wegobackend.group.v2.domain.entity.GroupImageV2;

public record GroupImageItem(
        Long groupImageId,
        int sortOrder,
        List<GroupImageVariantItem> variants
) {

    public static GroupImageItem from(GroupImageV2 image) {
        return new GroupImageItem(
                image.getId(),
                image.getSortOrder(),
                image.getVariants().stream().map(GroupImageVariantItem::from).toList()
        );
    }
}
