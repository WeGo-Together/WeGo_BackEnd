package team.wego.wegobackend.group.v2.domain.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter(AccessLevel.PUBLIC)
public enum GroupImageV2VariantType {
    CARD_440_240(440, 240, ImageV2Format.WEBP),
    THUMBNAIL_100_100(100, 100, ImageV2Format.WEBP);

    private final int width;
    private final int height;
    private final ImageV2Format defaultFormat;

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public ImageV2Format defaultFormat() {
        return defaultFormat;
    }
}
