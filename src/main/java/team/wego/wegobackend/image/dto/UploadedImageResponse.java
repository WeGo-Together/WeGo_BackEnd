package team.wego.wegobackend.image.dto;


import team.wego.wegobackend.common.infrastructure.aws.image.UploadedImage;

public record UploadedImageResponse(
        String key,
        String url
) {

    public static UploadedImageResponse from(UploadedImage image) {
        return new UploadedImageResponse(image.key(), image.url());
    }
}
