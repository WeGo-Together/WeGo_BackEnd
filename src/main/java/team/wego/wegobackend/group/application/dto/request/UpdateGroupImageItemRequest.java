package team.wego.wegobackend.group.application.dto.request;

public record UpdateGroupImageItemRequest(
        Integer sortOrder,
        String imageUrl440x240,
        String imageUrl100x100
) {

}


