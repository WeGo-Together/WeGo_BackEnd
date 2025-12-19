package team.wego.wegobackend.group.v2.application.dto.common;

import team.wego.wegobackend.group.v2.domain.entity.GroupImageV2Variant;
import team.wego.wegobackend.group.v2.domain.entity.GroupImageV2VariantType;
import team.wego.wegobackend.group.v2.domain.entity.ImageV2Format;

public record GroupImageVariantItem(
        Long variantId,
        GroupImageV2VariantType type,
        int width,
        int height,
        ImageV2Format format,
        String imageUrl
) {

    public static GroupImageVariantItem from(GroupImageV2Variant variant) {
        GroupImageV2VariantType type = variant.getType();
        return new GroupImageVariantItem(
                variant.getId(),
                type,
                type.width(),
                type.height(),
                variant.getFormat(),
                variant.getImageUrl()
        );
    }
}
