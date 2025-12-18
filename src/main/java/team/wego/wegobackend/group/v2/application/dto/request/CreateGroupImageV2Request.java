package team.wego.wegobackend.group.v2.application.dto.request;

public record CreateGroupImageV2Request(
        String imageKey,
        Integer sortOrder
) {}
