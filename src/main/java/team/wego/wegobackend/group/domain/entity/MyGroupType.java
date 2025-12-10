package team.wego.wegobackend.group.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;

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
            throw new IllegalArgumentException("MyGroupType 값은 null일 수 없습니다.");
        }
        return switch (value) {
            case "current" -> CURRENT;
            case "myPost"  -> MY_POST;
            case "past"    -> PAST;
            default -> throw new IllegalArgumentException("지원하지 않는 MyGroupType: " + value);
        };
    }
}
