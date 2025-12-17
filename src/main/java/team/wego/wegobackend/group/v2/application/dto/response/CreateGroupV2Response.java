package team.wego.wegobackend.group.v2.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import team.wego.wegobackend.group.v2.domain.entity.GroupImageV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupImageV2Variant;
import team.wego.wegobackend.group.v2.domain.entity.GroupImageV2VariantType;
import team.wego.wegobackend.group.v2.domain.entity.GroupTagV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Role;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Address;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;
import team.wego.wegobackend.group.v2.domain.entity.ImageV2Format;
import team.wego.wegobackend.tag.domain.entity.Tag;
import team.wego.wegobackend.user.domain.User;

public record CreateGroupV2Response(
        Long id,
        String title,
        GroupV2Status status,
        Address address,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<String> tags,
        String description,
        long participantCount,
        int maxParticipants,
        CreatedBy createdBy,
        Membership myMembership,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<GroupImageItem> images
) {

    public static CreateGroupV2Response from(GroupV2 group, User currentUser) {

        List<String> tagNames = group.getGroupTags().stream()
                .map(GroupTagV2::getTag)
                .map(Tag::getName)
                .toList();

        long attendCount = group.getUsers().stream()
                .filter(u -> u.getStatus() == GroupUserV2Status.ATTEND)
                .count();

        GroupUserV2 myGroupUser = group.getUsers().stream()
                .filter(groupUserV2 -> groupUserV2.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElse(null);

        return new CreateGroupV2Response(
                group.getId(),
                group.getTitle(),
                group.getStatus(),
                Address.from(group.getAddress()),
                group.getStartTime(),
                group.getEndTime(),
                tagNames,
                group.getDescription(),
                attendCount,
                group.getMaxParticipants(),
                CreatedBy.from(group.getHost()),
                myGroupUser == null ? null : Membership.from(myGroupUser),
                group.getCreatedAt(),
                group.getUpdatedAt(),
                group.getImages().stream().map(GroupImageItem::from).toList()
        );
    }

    public record Address(
            String location,
            String locationDetail
    ) {

        public static Address from(GroupV2Address address) {
            return new Address(address.getLocation(), address.getLocationDetail());
        }
    }

    public record CreatedBy(
            Long userId,
            String nickName,
            String profileImage,
            String profileMessage
    ) {

        public static CreatedBy from(User host) {
            return new CreatedBy(host.getId(), host.getNickName(), host.getProfileImage(),
                    host.getProfileMessage());
        }
    }

    public record Membership(
            Long groupUserId,
            Long userId,
            GroupUserV2Role role,
            GroupUserV2Status status,
            LocalDateTime joinedAt,
            LocalDateTime leftAt
    ) {

        public static Membership from(GroupUserV2 gu) {
            return new Membership(
                    gu.getId(),
                    gu.getUser().getId(),
                    gu.getGroupRole(),
                    gu.getStatus(),
                    gu.getJoinedAt(),
                    gu.getLeftAt()
            );
        }
    }


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
                    type.getWidth(),
                    type.getHeight(),
                    variant.getFormat(),
                    variant.getImageUrl()
            );
        }
    }
}
