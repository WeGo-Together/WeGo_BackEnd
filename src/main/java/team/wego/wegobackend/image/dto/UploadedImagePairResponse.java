package team.wego.wegobackend.image.dto;


import team.wego.wegobackend.common.infrastructure.aws.image.UploadedImagePair;

public record UploadedImagePairResponse(
        UploadedImageResponse original,
        UploadedImageResponse webpThumb
) {

    public static UploadedImagePairResponse from(UploadedImagePair pair) {
        return new UploadedImagePairResponse(
                UploadedImageResponse.from(pair.original()),
                UploadedImageResponse.from(pair.webpThumb())
        );
    }
}

