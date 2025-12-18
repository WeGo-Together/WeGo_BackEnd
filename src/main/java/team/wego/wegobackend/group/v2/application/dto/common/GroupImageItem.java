package team.wego.wegobackend.group.v2.application.dto.common;

import java.util.List;
import team.wego.wegobackend.group.v2.domain.entity.GroupImageV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupImageV2VariantType;
import team.wego.wegobackend.group.v2.domain.entity.ImageV2Format;

public record GroupImageItem(
        Long groupImageId,
        String imageKey,
        int sortOrder,
        List<GroupImageVariantItem> variants
) {

    private static final String DEFAULT_100 =
            "https://we-go-bucket.s3.ap-northeast-2.amazonaws.com/default/group_logo_100x100.webp";
    private static final String DEFAULT_440 =
            "https://we-go-bucket.s3.ap-northeast-2.amazonaws.com/default/group_logo_440x240.webp";

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
                null, // DB row가 아니다. 그래서 null
                "DEFAULT", // 테스트 편하게 DEFAULT로 설정
                0,    // 없으니까 어차피 대표
                List.of(
                        new GroupImageVariantItem(
                                null,
                                GroupImageV2VariantType.CARD_440_240,
                                GroupImageV2VariantType.CARD_440_240.getWidth(),
                                GroupImageV2VariantType.CARD_440_240.getHeight(),
                                ImageV2Format.WEBP,
                                DEFAULT_440
                        ),
                        new GroupImageVariantItem(
                                null,
                                GroupImageV2VariantType.THUMBNAIL_100_100,
                                GroupImageV2VariantType.THUMBNAIL_100_100.getWidth(),
                                GroupImageV2VariantType.THUMBNAIL_100_100.getHeight(),
                                ImageV2Format.WEBP,
                                DEFAULT_100
                        )
                )
        );
    }
}
