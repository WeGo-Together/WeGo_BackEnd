package team.wego.wegobackend.group.v2.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter(AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "v2_group_image_variants",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_image_id_variant_type",
                        columnNames = {"group_image_id", "variant_type"}
                )
        }
)
@Entity
public class GroupImageV2Variant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_image_id", nullable = false)
    private GroupImageV2 groupImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "variant_type", nullable = false, length = 30)
    private GroupImageV2VariantType type;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_format", nullable = false, length = 10)
    private ImageV2Format format;

    private GroupImageV2Variant(
            GroupImageV2VariantType type,
            String imageUrl,
            ImageV2Format format) {
        this.type = type;
        this.imageUrl = imageUrl;
        this.format = format;
    }

    public static GroupImageV2Variant create(
            GroupImageV2VariantType type,
            String imageUrl) {
        return new GroupImageV2Variant(type, imageUrl, type.defaultFormat());
    }

    public static GroupImageV2Variant create(
            GroupImageV2VariantType type,
            String imageUrl,
            ImageV2Format format) {
        return new GroupImageV2Variant(type, imageUrl, format);
    }

    void assignTo(GroupImageV2 groupImage) {
        this.groupImage = groupImage;
    }
}





