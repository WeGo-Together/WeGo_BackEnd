package team.wego.wegobackend.group.v2.application.dto.common;

import team.wego.wegobackend.group.v2.application.dto.response.GetGroupV2Response;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Address;

public record Address(String location, String locationDetail) {
    public static Address from(GroupV2Address address) {
        return new Address(address.getLocation(), address.getLocationDetail());
    }
}