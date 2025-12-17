package team.wego.wegobackend.group.v2.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;

@Getter(AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class GroupV2Address {

    @Column(name = "location", nullable = false, length = 100)
    private String location;

    @Column(name = "location_detail", length = 255)
    private String locationDetail;

    private GroupV2Address(String location, String locationDetail) {
        if (location == null || location.isBlank()) {
            throw new GroupException(GroupErrorCode.LOCATION_REQUIRED);
        }
        this.location = location;
        this.locationDetail = locationDetail;
    }

    public static GroupV2Address of(String location, String locationDetail) {
        return new GroupV2Address(location, locationDetail);
    }
}


