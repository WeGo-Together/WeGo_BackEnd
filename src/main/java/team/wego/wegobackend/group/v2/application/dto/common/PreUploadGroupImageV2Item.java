package team.wego.wegobackend.group.v2.application.dto.common;

public record PreUploadGroupImageV2Item(
        String imageKey,
        Integer sortOrder,
        String imageUrl440x240,
        String imageUrl100x100
) {

}
