package team.wego.wegobackend.common.infrastructure.aws.image;

public record UploadedImagePair(
        UploadedImage original,
        UploadedImage webpThumb
) {

}

