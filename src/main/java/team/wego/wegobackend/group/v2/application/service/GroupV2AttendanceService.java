package team.wego.wegobackend.group.v2.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.group.domain.exception.GroupErrorCode;
import team.wego.wegobackend.group.domain.exception.GroupException;
import team.wego.wegobackend.group.v2.application.dto.common.MyMembership;
import team.wego.wegobackend.group.v2.application.dto.response.ApproveRejectGroupV2Response;
import team.wego.wegobackend.group.v2.application.dto.response.AttendanceGroupV2Response;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Role;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2JoinPolicy;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;
import team.wego.wegobackend.group.v2.domain.repository.GroupUserV2Repository;
import team.wego.wegobackend.group.v2.domain.repository.GroupV2Repository;
import team.wego.wegobackend.user.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class GroupV2AttendanceService {

    private final GroupUserV2Repository groupUserV2Repository;
    private final GroupV2Repository groupV2Repository;

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

            // 정원 체크 수행. 재참여 포함해서 체크하는 게 안전
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
    public ApproveRejectGroupV2Response approve(Long approverUserId, Long groupId,
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

        return ApproveRejectGroupV2Response.of(group, attendCount, targetUserId, target);
    }

    @Transactional
    public ApproveRejectGroupV2Response reject(Long approverUserId, Long groupId,
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
        return ApproveRejectGroupV2Response.of(group, attendCount, targetUserId, target);
    }
}
