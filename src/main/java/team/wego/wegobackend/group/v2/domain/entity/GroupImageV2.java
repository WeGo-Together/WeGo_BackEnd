package team.wego.wegobackend.group.v2.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;


@Getter(AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "v2_group_images",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_id_sort_order", columnNames = {"group_id", "sort_order"}
                )
        }
)
@Entity
public class GroupImageV2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_image_id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "image_key", nullable = false, updatable = false, length = 36, unique = true)
    private String imageKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupV2 group;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @OneToMany(mappedBy = "groupImage", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<GroupImageV2Variant> variants = new ArrayList<>();

    private GroupImageV2(GroupV2 group, String imageKey, int sortOrder) {
        if (imageKey == null || imageKey.isBlank()) {
            throw new GroupException(GroupErrorCode.GROUP_IMAGE_KEY_REQUIRED);
        }
        this.imageKey = imageKey;
        this.sortOrder = sortOrder;
        assignTo(group);
        group.addImage(this);
    }

    public static GroupImageV2 create(
            GroupV2 group,
            int sortOrder,
            String imageKey,
            String url440x240,
            String url100x100
    ) {
        if (group == null) {
            throw new GroupException(GroupErrorCode.GROUP_NOT_NULL);
        }
        if (imageKey == null || imageKey.isBlank()) {
            throw new GroupException(GroupErrorCode.GROUP_IMAGE_KEY_REQUIRED);
        }
        if (url440x240 == null || url440x240.isBlank()) {
            throw new GroupException(GroupErrorCode.GROUP_IMAGE_MUST_440_240);
        }
        if (url100x100 == null || url100x100.isBlank()) {
            throw new GroupException(GroupErrorCode.GROUP_IMAGE_MUST_100_100);
        }

        GroupImageV2 image = new GroupImageV2(group, imageKey, sortOrder);

        image.addVariant(
                GroupImageV2Variant.create(GroupImageV2VariantType.CARD_440_240, url440x240));
        image.addVariant(
                GroupImageV2Variant.create(GroupImageV2VariantType.THUMBNAIL_100_100, url100x100));

        return image;
    }

    void assignTo(GroupV2 group) {
        this.group = group;
    }

    void unassign() {
        this.group = null;
    }

    public void addVariant(GroupImageV2Variant variant) {
        if (variant == null) {
            throw new GroupException(GroupErrorCode.GROUP_IMAGE_VARIANT_REQUIRED);
        }
        this.variants.add(variant);
        variant.assignTo(this);
    }

    public void changeSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}

