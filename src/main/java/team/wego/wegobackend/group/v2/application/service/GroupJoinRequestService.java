package team.wego.wegobackend.group.v2.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.common.security.CustomUserDetails;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;
import team.wego.wegobackend.group.v2.application.dto.common.JoinRequestItem;
import team.wego.wegobackend.group.v2.application.dto.response.GroupJoinRequestsResponse;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.group.v2.domain.repository.GroupImageV2QueryRepository;
import team.wego.wegobackend.group.v2.domain.repository.GroupUserV2QueryRepository;
import team.wego.wegobackend.group.v2.domain.repository.GroupV2Repository;

@Slf4j
@RequiredArgsConstructor
@Service
public class GroupJoinRequestService {

    private final GroupV2Repository groupV2Repository;
    private final GroupUserV2QueryRepository groupUserV2QueryRepository;
    private final GroupImageV2QueryRepository groupImageV2QueryRepository;


    @Transactional(readOnly = true)
    public GroupJoinRequestsResponse getJoinRequests(
            Long groupId,
            CustomUserDetails userDetails,
            GroupUserV2Status status
    ) {
        if (userDetails == null || userDetails.getId() == null) {
            throw new GroupException(GroupErrorCode.USER_ID_NULL);
        }

        // 모임 조회
        GroupV2 groupV2 = groupV2Repository.findById(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));

        // HOST 확인
        if (!groupV2.getHost().getId().equals(userDetails.getId())) {
            throw new GroupException(
                    GroupErrorCode.NO_PERMISSION_TO_VIEW_JOIN_REQUESTS,
                    groupId,
                    userDetails.getId()
            );
        }

        // status 기본값 방어
        GroupUserV2Status targetStatus = (status == null) ? GroupUserV2Status.PENDING : status;

        // QueryDSL 조회

        List<JoinRequestItem> items = groupUserV2QueryRepository
                .fetchJoinRequests(groupId, targetStatus)
                .stream()
                .map(joinRequestRow -> new JoinRequestItem(
                        joinRequestRow.userId(),
                        joinRequestRow.nickName(),
                        joinRequestRow.profileImage(),
                        joinRequestRow.groupUserId(),
                        joinRequestRow.status(),
                        joinRequestRow.joinedAt(),
                        joinRequestRow.joinRequestMessage()
                ))
                .toList();

        String thumbnail100Url = groupImageV2QueryRepository.fetchThumbnail100Url(groupId);

        return GroupJoinRequestsResponse.of(
                groupId,
                groupV2.getTitle(),
                thumbnail100Url,
                targetStatus,
                items
        );
    }
}
