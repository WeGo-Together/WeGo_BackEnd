package team.wego.wegobackend.group.v2.application.dto.common;

import java.io.Serializable;
import java.time.LocalDateTime;

public record PreUploadedGroupImage(
        String imageKey,
        Long uploaderId,
        String url440x240,
        String url100x100,
        LocalDateTime createdAt
) implements Serializable {

}
