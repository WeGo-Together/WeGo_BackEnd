package team.wego.wegobackend.group.application.dto.v1.response;

import java.util.List;

public record PreUploadGroupImageResponse(
        List<PreUploadGroupImageItemResponse> images
) {}
