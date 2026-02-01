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

    @Column(name = "latitude", length = 255)
    private Double latitude;

    @Column(name = "longitude", length = 255)
    private Double longitude;

    private GroupV2Address(String location, String locationDetail, Double latitude,
            Double longitude) {
        if (location == null || location.isBlank()) {
            throw new GroupException(GroupErrorCode.LOCATION_REQUIRED);
        }

        // 위/경도는 둘 다 있거나 둘 다 없어야 함
        if ((latitude == null) != (longitude == null)) {
            throw new GroupException(GroupErrorCode.LOCATION_COORDINATES_INVALID);
        }

        // 범위 검증(둘 다 있을 때만)
        if (latitude != null) {
            if (latitude < -90 || latitude > 90) {
                throw new GroupException(GroupErrorCode.LOCATION_LATITUDE_OUT_OF_RANGE);
            }
            if (longitude < -180 || longitude > 180) {
                throw new GroupException(GroupErrorCode.LOCATION_LONGITUDE_OUT_OF_RANGE);
            }
        }

        this.location = location.trim();
        this.locationDetail = locationDetail;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static GroupV2Address of(String location, String locationDetail, Double latitude,
            Double longitude) {
        return new GroupV2Address(location, locationDetail, latitude, longitude);
    }
}


