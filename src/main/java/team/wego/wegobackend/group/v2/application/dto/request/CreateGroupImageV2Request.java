package team.wego.wegobackend.group.v2.application.dto.request;

public record CreateGroupImageV2Request(
        Integer sortOrder,
        String imageUrl440x240,
        String imageUrl100x100
) {}
