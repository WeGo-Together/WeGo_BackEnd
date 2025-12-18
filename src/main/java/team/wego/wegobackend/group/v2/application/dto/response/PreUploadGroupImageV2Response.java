package team.wego.wegobackend.group.v2.application.dto.response;

import java.util.List;
import team.wego.wegobackend.group.v2.application.dto.common.PreUploadGroupImageV2Item;

public record PreUploadGroupImageV2Response(
        List<PreUploadGroupImageV2Item> images
) {

}