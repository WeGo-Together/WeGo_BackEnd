package team.wego.wegobackend.group.v2.application.dto.common;

import team.wego.wegobackend.group.v2.domain.entity.GroupV2Address;

public record Address(
        String location,
        String locationDetail,
        Double latitude,
        Double longitude
) {
    public static Address from(GroupV2Address address) {
        return new Address(
                address.getLocation(),
                address.getLocationDetail(),
                address.getLatitude(),
                address.getLongitude()
        );
    }
}
