package team.wego.wegobackend.group.v2.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;
import team.wego.wegobackend.group.v2.application.dto.common.AttendanceTargetItem;
import team.wego.wegobackend.group.v2.application.dto.common.MyMembership;
import team.wego.wegobackend.group.v2.application.dto.response.AttendanceGroupV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.GetBanTargetsResponse;
import team.wego.wegobackend.group.v2.application.dto.response.GetBannedTargetsResponse;
import team.wego.wegobackend.group.v2.application.dto.response.GetKickTargetsResponse;
import team.wego.wegobackend.group.v2.application.dto.response.GroupUserV2StatusResponse;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Role;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2JoinPolicy;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;
import team.wego.wegobackend.group.v2.domain.repository.GroupUserV2QueryRepository;
import team.wego.wegobackend.group.v2.domain.repository.GroupUserV2Repository;
import team.wego.wegobackend.group.v2.domain.repository.GroupV2Repository;
import team.wego.wegobackend.user.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class GroupV2AttendanceService {

    private final GroupUserV2Repository groupUserV2Repository;
    private final GroupV2Repository groupV2Repository;
    private final GroupUserV2QueryRepository groupUserV2QueryRepository;

    // 회원 호출
    private final UserRepository userRepository;


    // TODO: 참석, 취소 동시성 해결 필요.
    @Transactional
    public AttendanceGroupV2Response attend(Long userId, Long groupId) {
        // 회원 체크
        if (userId == null) {
            throw new GroupException(GroupErrorCode.USER_ID_NULL);
        }

        // 모임 체크
        GroupV2 group = groupV2Repository.findById(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));

        // HOST는 참석 불가능(모임 생성 시 이미 참가된 상태)
        if (group.getHost().getId().equals(userId)) {
            throw new GroupException(GroupErrorCode.GROUP_HOST_CANNOT_ATTEND);
        }

        // 모임 상태 체크
        if (group.getStatus() != GroupV2Status.RECRUITING) {
            throw new GroupException(GroupErrorCode.GROUP_NOT_RECRUITING, group.getStatus().name());
        }

        // 기존 멤버십 조회
        GroupUserV2 groupUserV2 = groupUserV2Repository.findByGroupIdAndUserId(groupId, userId)
                .orElse(null);

        if (groupUserV2 != null) {
            // BAN 차단
            if (groupUserV2.getStatus() == GroupUserV2Status.BANNED) {
                throw new GroupException(GroupErrorCode.GROUP_BANNED_USER);
            }

            // 이미 참석중이면 충돌
            if (groupUserV2.getStatus() == GroupUserV2Status.ATTEND) {
                throw new GroupException(GroupErrorCode.ALREADY_ATTEND_GROUP, groupId, userId);
            }

            // 이미 신청(PENDING)도 중복
            if (groupUserV2.getStatus() == GroupUserV2Status.PENDING) {
                throw new GroupException(GroupErrorCode.ALREADY_REQUESTED_TO_JOIN, groupId, userId);
            }

            // 나머지(LEFT, KICKED, REJECTED, CANCELLED)는 정책에 따라 재참여/재신청 허용
        }

        GroupV2JoinPolicy joinPolicy = group.getJoinPolicy();

        // 모임 참여 정책 NULL 체크
        if (joinPolicy == null) {
            throw new GroupException(GroupErrorCode.JOIN_POLICY_NULL);
        }

        // 즉시 참여인 경우
        if (group.getJoinPolicy() == GroupV2JoinPolicy.FREE) {
            if (groupUserV2 != null) {
                // LEFT, KICKED, REJECTED, CANCELLED -> 재참여
                groupUserV2.reAttend(); // 내부에서 BANNED만 막고, ATTEND로 변경
            } else {
                groupUserV2 = GroupUserV2.create(group, userRepository.getReferenceById(userId),
                        GroupUserV2Role.MEMBER);
                groupUserV2Repository.save(groupUserV2);
            }

            // 정원 체크(ATTEND만 카운트)
            long attendCount = groupUserV2Repository.countByGroupIdAndStatus(groupId,
                    GroupUserV2Status.ATTEND);
            if (attendCount > group.getMaxParticipants()) {
                throw new GroupException(GroupErrorCode.GROUP_IS_FULL, groupId);
            }

            // FULL 자동 전환
            if (attendCount == group.getMaxParticipants()
                    && group.getStatus() == GroupV2Status.RECRUITING) {
                group.changeStatus(GroupV2Status.FULL);
            }

            MyMembership membership = MyMembership.from(groupUserV2);
            return AttendanceGroupV2Response.of(group, attendCount, membership);
        }

        if (joinPolicy == GroupV2JoinPolicy.APPROVAL_REQUIRED) {
            if (groupUserV2 != null) {
                // LEFT, KICKED, REJECTED, CANCELLED -> 재신청(PENDING)으로 전환
                // reAttend()는 ATTEND로 바꾸므로 승인제에서는 쓰지 않는게 안전하다고 판단
                groupUserV2.requestJoin();
            } else {
                groupUserV2 = GroupUserV2.createPending(
                        group,
                        userRepository.getReferenceById(userId)
                );
                groupUserV2Repository.save(groupUserV2);
            }

            // 승인을 할 때만 정원 체크(이미 approve에서 하고 있으니 충분)
            // approval_required에서 attend는 정원 체크 생략 가능
            long attendCount = groupUserV2Repository.countByGroupIdAndStatus(
                    groupId,
                    GroupUserV2Status.ATTEND);
            if (attendCount > group.getMaxParticipants()) {
                // 방금 reAttend로 늘었는데 초과하면 롤백시키기 위해 예외
                throw new GroupException(GroupErrorCode.GROUP_IS_FULL, groupId);
            }

            // FULL 자동 전환
            if (attendCount == group.getMaxParticipants()
                    && group.getStatus() == GroupV2Status.RECRUITING) {
                group.changeStatus(GroupV2Status.FULL);
            }

            // 내 멤버십 + 최신 카운트 + 모임 상태 응답
            MyMembership membership = MyMembership.from(groupUserV2);

            return AttendanceGroupV2Response.of(group, attendCount, membership);
        }
        throw new GroupException(GroupErrorCode.INVALID_JOIN_POLICY, String.valueOf(joinPolicy));
    }

    @Transactional
    public AttendanceGroupV2Response left(Long userId, Long groupId) {
        if (userId == null) {
            throw new GroupException(GroupErrorCode.USER_ID_NULL);
        }

        GroupV2 group = groupV2Repository.findById(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID,
                                groupId));

        // HOST는 나가기/신청취소 불가
        if (group.getHost().getId().equals(userId)) {
            throw new GroupException(GroupErrorCode.GROUP_HOST_CANNOT_LEAVE, groupId, userId);
        }

        if (!group.getStatus().canLeaveOrCancel()) {
            throw new GroupException(
                    GroupErrorCode.GROUP_CANNOT_LEAVE_IN_STATUS,
                    groupId,
                    group.getStatus().name()
            );
        }

        GroupUserV2 groupUserV2 = groupUserV2Repository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_USER_NOT_FOUND, userId));

        // 멤버십 상태 기반 정책, 행동은 도메인으로 위임
        groupUserV2.leaveOrCancel();

        // 참석 인원은 ATTEND만 카운트
        long attendCount = groupUserV2Repository.countByGroupIdAndStatus(groupId,
                GroupUserV2Status.ATTEND);

        // FULL -> RECRUITING 자동 복귀(선택)
        if (group.getStatus() == GroupV2Status.FULL
                && attendCount < group.getMaxParticipants()) {
            group.changeStatus(GroupV2Status.RECRUITING);
        }

        // 응답
        MyMembership membership = MyMembership.from(groupUserV2);

        return AttendanceGroupV2Response.of(group, attendCount, membership);
    }

    @Transactional
    public GroupUserV2StatusResponse approve(Long approverUserId, Long groupId,
            Long targetUserId) {
        if (approverUserId == null || targetUserId == null) {
            throw new GroupException(GroupErrorCode.USER_ID_NULL);
        }

        if (approverUserId.equals(targetUserId)) {
            throw new GroupException(GroupErrorCode.CANNOT_APPROVE_SELF, groupId, approverUserId);
        }

        GroupV2 group = groupV2Repository.findById(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));

        // 승인제 모임만 가능
        if (group.getJoinPolicy() != GroupV2JoinPolicy.APPROVAL_REQUIRED) {
            throw new GroupException(GroupErrorCode.GROUP_JOIN_POLICY_NOT_APPROVAL_REQUIRED,
                    groupId);
        }

        // 모임 상태 정책
        if (group.getStatus() != GroupV2Status.RECRUITING
                && group.getStatus() != GroupV2Status.FULL) {
            throw new GroupException(
                    GroupErrorCode.GROUP_CANNOT_APPROVE_IN_STATUS,
                    groupId,
                    group.getStatus().name()
            );
        }

        // 권한 체크: HOST 또는 (그룹 내 MANAGER/… 정책)만 승인 가능
        // host는 group.getHost()로 체크
        boolean isHost = group.getHost().getId().equals(approverUserId);
        boolean canApprove = isHost;

        if (!isHost) {
            GroupUserV2 approverMembership = groupUserV2Repository.findByGroupIdAndUserId(groupId,
                            approverUserId)
                    .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_USER_NOT_FOUND,
                            approverUserId));

            canApprove = approverMembership.getGroupRole() != null
                    && approverMembership.getGroupRole() != GroupUserV2Role.MEMBER;
        }

        if (!canApprove) {
            throw new GroupException(GroupErrorCode.NO_PERMISSION_TO_APPROVE_JOIN, groupId,
                    approverUserId);
        }

        GroupUserV2 target = groupUserV2Repository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_USER_NOT_FOUND,
                        targetUserId));

        // PENDING만 승인 가능 (도메인에서 검증)
        target.approveJoin();

        long attendCount = groupUserV2Repository.countByGroupIdAndStatus(groupId,
                GroupUserV2Status.ATTEND);

        if (attendCount > group.getMaxParticipants()) {
            throw new GroupException(GroupErrorCode.GROUP_IS_FULL, groupId);
        }

        // FULL 자동 전환
        if (attendCount == group.getMaxParticipants()
                && group.getStatus() == GroupV2Status.RECRUITING) {
            group.changeStatus(GroupV2Status.FULL);
        }

        return GroupUserV2StatusResponse.of(group, attendCount, targetUserId, target);
    }

    @Transactional
    public GroupUserV2StatusResponse reject(Long approverUserId, Long groupId,
            Long targetUserId) {
        if (approverUserId == null || targetUserId == null) {
            throw new GroupException(GroupErrorCode.USER_ID_NULL);
        }

        if (approverUserId.equals(targetUserId)) {
            throw new GroupException(GroupErrorCode.CANNOT_REJECT_SELF, groupId, approverUserId);
        }

        GroupV2 group = groupV2Repository.findById(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));

        if (group.getJoinPolicy() != GroupV2JoinPolicy.APPROVAL_REQUIRED) {
            throw new GroupException(GroupErrorCode.GROUP_JOIN_POLICY_NOT_APPROVAL_REQUIRED,
                    groupId);
        }

        if (group.getStatus() != GroupV2Status.RECRUITING
                && group.getStatus() != GroupV2Status.FULL) {
            throw new GroupException(
                    GroupErrorCode.GROUP_CANNOT_REJECT_IN_STATUS,
                    groupId,
                    group.getStatus().name()
            );
        }

        boolean isHost = group.getHost().getId().equals(approverUserId);
        boolean canReject = isHost;

        if (!isHost) {
            GroupUserV2 approverMembership = groupUserV2Repository.findByGroupIdAndUserId(groupId,
                            approverUserId)
                    .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_USER_NOT_FOUND,
                            approverUserId));

            canReject = approverMembership.getGroupRole() != null
                    && approverMembership.getGroupRole() != GroupUserV2Role.MEMBER; // 예시
        }

        if (!canReject) {
            throw new GroupException(GroupErrorCode.NO_PERMISSION_TO_REJECT_JOIN, groupId,
                    approverUserId);
        }

        GroupUserV2 target = groupUserV2Repository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_USER_NOT_FOUND,
                        targetUserId));

        target.rejectJoin();

        long attendCount = groupUserV2Repository.countByGroupIdAndStatus(groupId,
                GroupUserV2Status.ATTEND);

        // reject는 ATTEND 수가 바뀌지 않는 게 일반적이지만(대상은 PENDING),
        // 혹시라도 상태 정책이 바뀌더라도 count는 최신으로 내려가도록 유지
        return GroupUserV2StatusResponse.of(group, attendCount, targetUserId, target);
    }

    @Transactional
    public GroupUserV2StatusResponse kick(Long kickerUserId, Long groupId, Long targetUserId) {
        if (kickerUserId == null || targetUserId == null) {
            throw new GroupException(GroupErrorCode.USER_ID_NULL);
        }

        if (kickerUserId.equals(targetUserId)) {
            throw new GroupException(GroupErrorCode.GROUP_CANNOT_KICK_SELF, groupId, kickerUserId);
        }

        GroupV2 group = groupV2Repository.findById(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));

        // ✅ HOST only
        if (!group.getHost().getId().equals(kickerUserId)) {
            throw new GroupException(GroupErrorCode.NO_PERMISSION_TO_KICK, groupId, kickerUserId);
        }

        if (group.getHost().getId().equals(targetUserId)) {
            throw new GroupException(GroupErrorCode.GROUP_CANNOT_KICK_HOST, groupId, targetUserId);
        }

        if (group.getStatus() != GroupV2Status.RECRUITING
                && group.getStatus() != GroupV2Status.FULL) {
            throw new GroupException(GroupErrorCode.GROUP_CANNOT_KICK_IN_STATUS, groupId,
                    group.getStatus().name());
        }

        GroupUserV2 target = groupUserV2Repository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_USER_NOT_FOUND,
                        targetUserId));

        if (target.getStatus() != GroupUserV2Status.ATTEND) {
            throw new GroupException(
                    GroupErrorCode.GROUP_USER_STATUS_NOT_ALLOWED_TO_KICK,
                    groupId, targetUserId, target.getStatus().name()
            );
        }

        target.kick();

        long attendCount = groupUserV2Repository.countByGroupIdAndStatus(groupId,
                GroupUserV2Status.ATTEND);

        if (group.getStatus() == GroupV2Status.FULL && attendCount < group.getMaxParticipants()) {
            group.changeStatus(GroupV2Status.RECRUITING);
        }

        return GroupUserV2StatusResponse.of(group, attendCount, targetUserId, target);
    }

    @Transactional
    public GroupUserV2StatusResponse ban(Long bannerUserId, Long groupId, Long targetUserId) {
        if (bannerUserId == null || targetUserId == null) {
            throw new GroupException(GroupErrorCode.USER_ID_NULL);
        }

        if (bannerUserId.equals(targetUserId)) {
            throw new GroupException(GroupErrorCode.GROUP_CANNOT_BAN_SELF, groupId, bannerUserId);
        }

        GroupV2 group = groupV2Repository.findById(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));

        // HOST only
        if (!group.getHost().getId().equals(bannerUserId)) {
            throw new GroupException(GroupErrorCode.NO_PERMISSION_TO_BAN, groupId, bannerUserId);
        }

        if (group.getHost().getId().equals(targetUserId)) {
            throw new GroupException(GroupErrorCode.GROUP_CANNOT_BAN_HOST, groupId, targetUserId);
        }

        if (group.getStatus() != GroupV2Status.RECRUITING
                && group.getStatus() != GroupV2Status.FULL) {
            throw new GroupException(GroupErrorCode.GROUP_CANNOT_BAN_IN_STATUS, groupId,
                    group.getStatus().name());
        }

        GroupUserV2 target = groupUserV2Repository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_USER_NOT_FOUND,
                        targetUserId));

        if (target.getStatus() != GroupUserV2Status.ATTEND) {
            throw new GroupException(
                    GroupErrorCode.GROUP_USER_STATUS_NOT_ALLOWED_TO_BAN,
                    groupId, targetUserId, target.getStatus().name()
            );
        }

        target.ban();

        long attendCount = groupUserV2Repository.countByGroupIdAndStatus(groupId,
                GroupUserV2Status.ATTEND);

        if (group.getStatus() == GroupV2Status.FULL && attendCount < group.getMaxParticipants()) {
            group.changeStatus(GroupV2Status.RECRUITING);
        }

        return GroupUserV2StatusResponse.of(group, attendCount, targetUserId, target);
    }

    @Transactional(readOnly = true)
    public GetKickTargetsResponse getKickTargets(Long requesterUserId, Long groupId) {
        if (requesterUserId == null) {
            throw new GroupException(GroupErrorCode.USER_ID_NULL);
        }

        GroupV2 group = groupV2Repository.findById(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));

        // HOST만 조회 가능
        if (!group.getHost().getId().equals(requesterUserId)) {
            throw new GroupException(GroupErrorCode.NO_PERMISSION_TO_VIEW_KICK_TARGETS, groupId,
                    requesterUserId);
        }

        List<AttendanceTargetItem> targets = groupUserV2QueryRepository
                .fetchAttendMembersExceptHost(groupId)
                .stream()
                .map(r -> new AttendanceTargetItem(
                        r.userId(),
                        r.nickName(),
                        r.profileImage(),
                        r.groupUserId(),
                        r.status(),
                        r.joinedAt()
                ))
                .toList();

        return GetKickTargetsResponse.of(groupId, targets);
    }

    @Transactional(readOnly = true)
    public GetBanTargetsResponse getBanTargets(Long requesterUserId, Long groupId) {
        if (requesterUserId == null) {
            throw new GroupException(GroupErrorCode.USER_ID_NULL);
        }

        GroupV2 group = groupV2Repository.findById(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));

        if (!group.getHost().getId().equals(requesterUserId)) {
            throw new GroupException(GroupErrorCode.NO_PERMISSION_TO_VIEW_BAN_TARGETS, groupId,
                    requesterUserId);
        }

        List<AttendanceTargetItem> targets = groupUserV2QueryRepository
                .fetchAttendMembersExceptHost(groupId)
                .stream()
                .map(r -> new AttendanceTargetItem(
                        r.userId(),
                        r.nickName(),
                        r.profileImage(),
                        r.groupUserId(),
                        r.status(),
                        r.joinedAt()
                ))
                .toList();

        return GetBanTargetsResponse.of(groupId, targets);
    }

    @Transactional
    public GroupUserV2StatusResponse unban(Long requesterUserId, Long groupId, Long targetUserId) {
        if (requesterUserId == null || targetUserId == null) {
            throw new GroupException(GroupErrorCode.USER_ID_NULL);
        }

        GroupV2 group = groupV2Repository.findById(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));

        // HOST만 가능
        if (!group.getHost().getId().equals(requesterUserId)) {
            throw new GroupException(GroupErrorCode.NO_PERMISSION_TO_UNBAN, groupId,
                    requesterUserId);
        }

        // HOST는 unban 대상이 될 수 없음(애초에 ban도 불가지만 방어)
        if (group.getHost().getId().equals(targetUserId)) {
            throw new GroupException(GroupErrorCode.GROUP_CANNOT_UNBAN_HOST, groupId, targetUserId);
        }

        GroupUserV2 target = groupUserV2Repository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_USER_NOT_FOUND,
                        targetUserId));

        target.unban(); // BANNED만 허용, KICKED로 전환

        long attendCount = groupUserV2Repository.countByGroupIdAndStatus(groupId,
                GroupUserV2Status.ATTEND);

        // 상태 복귀는 unban에서 인원수가 바뀌지 않지만(어차피 BANNED는 ATTEND가 아님),
        // 안전하게 FULL 복귀 로직은 건드릴 필요 없음.
        return GroupUserV2StatusResponse.of(group, attendCount, targetUserId, target);
    }

    @Transactional(readOnly = true)
    public GetBannedTargetsResponse getBannedTargets(Long requesterUserId, Long groupId) {
        if (requesterUserId == null) {
            throw new GroupException(GroupErrorCode.USER_ID_NULL);
        }

        GroupV2 group = groupV2Repository.findById(groupId)
                .orElseThrow(
                        () -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND_BY_ID, groupId));

        // HOST만 조회 가능
        if (!group.getHost().getId().equals(requesterUserId)) {
            throw new GroupException(GroupErrorCode.NO_PERMISSION_TO_VIEW_BANNED_TARGETS, groupId,
                    requesterUserId);
        }

        List<AttendanceTargetItem> targets = groupUserV2QueryRepository
                .fetchBannedMembersExceptHost(groupId)
                .stream()
                .map(r -> new AttendanceTargetItem(
                        r.userId(),
                        r.nickName(),
                        r.profileImage(),
                        r.groupUserId(),
                        r.status(),
                        r.joinedAt()
                ))
                .toList();

        return GetBannedTargetsResponse.of(groupId, targets);
    }
}
