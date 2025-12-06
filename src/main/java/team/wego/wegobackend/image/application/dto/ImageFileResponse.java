package team.wego.wegobackend.image.application.dto;

import team.wego.wegobackend.image.domain.ImageFile;

public record ImageFileResponse(
        String key,
        String url
) {

    public static ImageFileResponse from(ImageFile file) {
        return new ImageFileResponse(file.key(), file.url());
    }
}
