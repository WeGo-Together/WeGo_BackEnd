package team.wego.wegobackend.group.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;

@Getter(AccessLevel.PUBLIC)
public enum MyGroupType {
    CURRENT("current"),
    MY_POST("myPost"),
    PAST("past");

    private final String value;

    MyGroupType(String value) {
        this.value = value;
    }

    public static MyGroupType from(String value) {
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
