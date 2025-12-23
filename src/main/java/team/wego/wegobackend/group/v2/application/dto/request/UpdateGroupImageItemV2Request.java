package team.wego.wegobackend.group.v2.application.dto.request;

public record UpdateGroupImageItemV2Request(
        Long id,                // 기존 이미지면 id
        String imageKey,        // 신규면 imageKey
        Integer sortOrder,      // 프론트가 보낸 순서(선택)
        String imageUrl440x240, // 신규일 때만 필요
        String imageUrl100x100
) {

}
