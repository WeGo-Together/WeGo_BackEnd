package team.wego.wegobackend.group.v2.domain.entity;

public enum GroupV2JoinPolicy {
    INSTANT,              // 참여 버튼 누르면 즉시 ATTEND
    APPROVAL_REQUIRED,    // 참여 버튼 누르면 신청 상태로 들어가고 HOST 승인 후 ATTEND
    INVITE_ONLY           // (미정)초대 받은 사람만 참여
}
