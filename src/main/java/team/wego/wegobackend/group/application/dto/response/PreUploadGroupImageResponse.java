package team.wego.wegobackend.group.application.dto.response;

import java.util.List;

public record PreUploadGroupImageResponse(
        List<PreUploadGroupImageItemResponse> images
) {}
