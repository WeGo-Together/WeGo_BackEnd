package team.wego.wegobackend.group.v2.infrastructure.querydsl;

import static team.wego.wegobackend.group.v2.domain.entity.QGroupUserV2.groupUserV2;
import static team.wego.wegobackend.user.domain.QUser.user;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Role;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;
import team.wego.wegobackend.group.v2.domain.repository.GroupUserV2QueryRepository;
import team.wego.wegobackend.group.v2.infrastructure.querydsl.projection.AttendanceTargetRow;
import team.wego.wegobackend.group.v2.infrastructure.querydsl.projection.JoinRequestRow;

@RequiredArgsConstructor
@Repository
public class GroupUserV2QueryRepositoryImpl implements GroupUserV2QueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<AttendanceTargetRow> fetchAttendMembersExceptHost(Long groupId) {
        return queryFactory
                .select(Projections.constructor(
                        AttendanceTargetRow.class,
                        user.id,
                        user.nickName,
                        user.profileImage,
                        groupUserV2.id,
                        groupUserV2.status,
                        groupUserV2.joinedAt,
                        groupUserV2.leftAt
                ))
                .from(groupUserV2)
                .join(groupUserV2.user, user)
                .where(
                        groupUserV2.group.id.eq(groupId),
                        groupUserV2.status.eq(GroupUserV2Status.ATTEND),
                        groupUserV2.groupRole.ne(
                                team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Role.HOST)
                )
                .orderBy(groupUserV2.joinedAt.desc())
                .fetch();
    }

    @Override
    public List<AttendanceTargetRow> fetchBannedMembersExceptHost(Long groupId) {
        return queryFactory
                .select(Projections.constructor(
                        AttendanceTargetRow.class,
                        user.id,
                        user.nickName,
                        user.profileImage,
                        groupUserV2.id,
                        groupUserV2.status,
                        groupUserV2.joinedAt,
                        groupUserV2.leftAt
                ))
                .from(groupUserV2)
                .join(groupUserV2.user, user)
                .where(
                        groupUserV2.group.id.eq(groupId),
                        groupUserV2.status.eq(GroupUserV2Status.BANNED),
                        groupUserV2.groupRole.ne(
                                team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Role.HOST)
                )
                .orderBy(groupUserV2.leftAt.desc().nullsLast())
                .fetch();
    }

    @Override
    public List<JoinRequestRow> fetchJoinRequests(Long groupId, GroupUserV2Status status) {
        return queryFactory
                .select(Projections.constructor(
                        JoinRequestRow.class,
                        user.id,
                        user.nickName,
                        user.profileImage,
                        groupUserV2.id,
                        groupUserV2.status,
                        groupUserV2.joinedAt,
                        groupUserV2.joinRequestMessage
                ))
                .from(groupUserV2)
                .join(groupUserV2.user, user)
                .where(
                        groupUserV2.group.id.eq(groupId),
                        groupUserV2.status.eq(status),
                        groupUserV2.groupRole.ne(GroupUserV2Role.HOST) // HOST 제외
                )
                // joinRequest는 최신 신청이 위로 오도록
                .orderBy(groupUserV2.joinedAt.desc())
                .fetch();
    }
}
