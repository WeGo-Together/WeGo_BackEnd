package team.wego.wegobackend.group.v2.application.dto.request;

import lombok.Getter;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;

@Getter
public enum MyGroupTypeV2 {
    CURRENT("current"),
    MY_POST("myPost"),
    PAST("past");

    private final String value;

    MyGroupTypeV2(String value) {
        this.value = value;
    }

    public static MyGroupTypeV2 from(String value) {
        if (value == null) {
            throw new GroupException(GroupErrorCode.MY_GROUP_TYPE_NOT_NULL);
        }
        return switch (value) {
            case "current" -> CURRENT;
            case "myPost" -> MY_POST;
            case "past" -> PAST;
            default -> throw new GroupException(GroupErrorCode.INVALID_MY_GROUP_TYPE, value);
        };
    }
}
