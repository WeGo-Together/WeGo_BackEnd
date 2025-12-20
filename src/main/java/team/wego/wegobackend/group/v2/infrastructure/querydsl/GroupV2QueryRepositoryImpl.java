package team.wego.wegobackend.group.v2.infrastructure.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import team.wego.wegobackend.group.v2.domain.entity.GroupImageV2VariantType;
import team.wego.wegobackend.group.v2.domain.entity.GroupUserV2Status;
import team.wego.wegobackend.group.v2.domain.entity.GroupV2Status;
import team.wego.wegobackend.group.v2.domain.entity.QGroupImageV2;
import team.wego.wegobackend.group.v2.domain.entity.QGroupImageV2Variant;
import team.wego.wegobackend.group.v2.domain.entity.QGroupTagV2;
import team.wego.wegobackend.group.v2.domain.entity.QGroupUserV2;
import team.wego.wegobackend.group.v2.domain.entity.QGroupV2;
import team.wego.wegobackend.group.v2.domain.repository.GroupV2QueryRepository;
import team.wego.wegobackend.group.v2.infrastructure.querydsl.projection.GroupListRow;
import team.wego.wegobackend.group.v2.infrastructure.querydsl.projection.MyGroupListRow;
import team.wego.wegobackend.tag.domain.entity.QTag;
import team.wego.wegobackend.user.domain.QUser;


@RequiredArgsConstructor
@Repository
public class GroupV2QueryRepositoryImpl implements GroupV2QueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<GroupListRow> fetchGroupRows(
            String keyword,
            Long cursor,
            int limit,
            List<GroupV2Status> includeStatuses,
            List<GroupV2Status> excludeStatuses
    ) {
        QGroupV2 group = QGroupV2.groupV2;
        QUser user = QUser.user;
        QGroupUserV2 groupUser = QGroupUserV2.groupUserV2;

        BooleanBuilder where = new BooleanBuilder();
        where.and(group.deletedAt.isNull());

        if (includeStatuses != null && !includeStatuses.isEmpty()) {
            where.and(group.status.in(includeStatuses));
        }

        if (excludeStatuses != null && !excludeStatuses.isEmpty()) {
            where.and(group.status.notIn(excludeStatuses));
        }

        if (keyword != null && !keyword.trim().isBlank()) {
            String key = keyword.trim();
            where.and(
                    group.title.containsIgnoreCase(key)
                            .or(group.address.location.containsIgnoreCase(key))
                            .or(group.description.containsIgnoreCase(key))
                            // 필요하면 상세주소도 검색 포함 가능
                            .or(group.address.locationDetail.containsIgnoreCase(key))
            );
        }

        if (cursor != null) {
            where.and(group.id.lt(cursor));
        }

        return queryFactory
                .select(Projections.constructor(
                        GroupListRow.class,
                        group.id,
                        group.title,
                        group.status,
                        group.address.location,
                        group.address.locationDetail,
                        group.startTime,
                        group.endTime,
                        group.description,
                        group.maxParticipants,
                        group.createdAt,
                        group.updatedAt,
                        user.id,
                        user.nickName,
                        user.profileImage,
                        groupUser.id.count()
                ))
                .from(group)
                .join(group.host, user)
                .leftJoin(group.users, groupUser)
                .on(groupUser.status.eq(GroupUserV2Status.ATTEND))
                .where(where)
                .groupBy(
                        group.id,
                        group.title,
                        group.status,
                        group.address.location,
                        group.address.locationDetail,
                        group.startTime,
                        group.endTime,
                        group.description,
                        group.maxParticipants,
                        group.createdAt,
                        group.updatedAt,
                        user.id,
                        user.nickName,
                        user.profileImage
                )
                .orderBy(group.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public Map<Long, List<String>> fetchTagNamesByGroupIds(List<Long> groupIds) {
        if (groupIds == null || groupIds.isEmpty()) {
            return Map.of();
        }

        QGroupTagV2 groupTagV2 = QGroupTagV2.groupTagV2;
        QTag tag = QTag.tag;

        List<Tuple> tuples = queryFactory
                .select(groupTagV2.group.id, tag.name)
                .from(groupTagV2)
                .join(groupTagV2.tag, tag)
                .where(groupTagV2.group.id.in(groupIds))
                .fetch();

        return tuples.stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(groupTagV2.group.id),
                        Collectors.mapping(tuple -> tuple.get(tag.name),
                                Collectors.toList())
                ));
    }


    @Override
    public Map<Long, List<String>> fetchMainImageUrlsByGroupIds(List<Long> groupIds,
            int perGroupLimit
    ) {
        if (groupIds == null || groupIds.isEmpty()) {
            return Map.of();
        }

        QGroupImageV2 groupImageV2 = QGroupImageV2.groupImageV2;
        QGroupImageV2Variant groupImageV2Variant = QGroupImageV2Variant.groupImageV2Variant;

        // CARD_440_240만 뽑기
        List<Tuple> tuples = queryFactory
                .select(groupImageV2.group.id, groupImageV2.sortOrder, groupImageV2Variant.imageUrl)
                .from(groupImageV2Variant)
                .join(groupImageV2Variant.groupImage, groupImageV2)
                .where(
                        groupImageV2.group.id.in(groupIds),
                        groupImageV2Variant.type.eq(GroupImageV2VariantType.CARD_440_240)
                )
                .orderBy(groupImageV2.group.id.desc(),
                        groupImageV2.sortOrder.asc(),
                        groupImageV2Variant.id.asc())
                .fetch();

        Map<Long, List<String>> result = new HashMap<>();

        for (Tuple tuple : tuples) {
            Long groupId = tuple.get(groupImageV2.group.id);
            String url = tuple.get(groupImageV2Variant.imageUrl);
            if (groupId == null || url == null || url.isBlank()) {
                continue;
            }

            List<String> list = result.computeIfAbsent(groupId, k -> new ArrayList<>());
            if (list.size() < perGroupLimit) {
                list.add(url);
            }
        }

        return result;
    }

    @Override
    public List<MyGroupListRow> fetchMyGroupRows(
            Long userId,
            Long cursor,
            int limit,
            List<GroupV2Status> includeStatuses,
            List<GroupV2Status> excludeStatuses,
            List<GroupUserV2Status> myStatuses
    ) {
        QGroupV2 group = QGroupV2.groupV2;
        QUser host = QUser.user;

        QGroupUserV2 myGu = new QGroupUserV2("myGu");
        QGroupUserV2 guAttend = new QGroupUserV2("guAttend");

        BooleanBuilder where = new BooleanBuilder();
        where.and(group.deletedAt.isNull());
        where.and(myGu.user.id.eq(userId));

        if (myStatuses != null && !myStatuses.isEmpty()) {
            where.and(myGu.status.in(myStatuses));
        } else {
            where.and(myGu.status.eq(GroupUserV2Status.ATTEND));
        }

        if (includeStatuses != null && !includeStatuses.isEmpty()) {
            where.and(group.status.in(includeStatuses));
        }
        if (excludeStatuses != null && !excludeStatuses.isEmpty()) {
            where.and(group.status.notIn(excludeStatuses));
        }
        if (cursor != null) {
            where.and(group.id.lt(cursor));
        }

        return queryFactory
                .select(Projections.constructor(
                        MyGroupListRow.class,
                        group.id,
                        group.title,
                        group.status,
                        group.address.location,
                        group.address.locationDetail,
                        group.startTime,
                        group.endTime,
                        group.description,
                        group.maxParticipants,
                        group.createdAt,
                        group.updatedAt,

                        host.id,
                        host.nickName,
                        host.profileImage,
                        host.profileMessage, // 추가

                        guAttend.id.count(), // ATTEND만 카운트

                        myGu.id,
                        myGu.groupRole,
                        myGu.status,
                        myGu.joinedAt,
                        myGu.leftAt
                ))
                .from(group)
                .join(group.host, host)

                .join(group.users, myGu) // 내 membership 기반 (inner join)

                .leftJoin(group.users, guAttend)
                .on(guAttend.status.eq(GroupUserV2Status.ATTEND))

                .where(where)
                .groupBy(
                        group.id,
                        group.title,
                        group.status,
                        group.address.location,
                        group.address.locationDetail,
                        group.startTime,
                        group.endTime,
                        group.description,
                        group.maxParticipants,
                        group.createdAt,
                        group.updatedAt,

                        host.id,
                        host.nickName,
                        host.profileImage,
                        host.profileMessage,

                        myGu.id,
                        myGu.groupRole,
                        myGu.status,
                        myGu.joinedAt,
                        myGu.leftAt
                )
                .orderBy(group.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<MyGroupListRow> fetchMyPostGroupRows(
            Long userId,
            Long cursor,
            int limit,
            List<GroupV2Status> includeStatuses,
            List<GroupV2Status> excludeStatuses
    ) {
        QGroupV2 group = QGroupV2.groupV2;
        QUser host = QUser.user;

        QGroupUserV2 myGu = new QGroupUserV2("myGu");
        QGroupUserV2 guAttend = new QGroupUserV2("guAttend");

        BooleanBuilder where = new BooleanBuilder();
        where.and(group.deletedAt.isNull());
        where.and(group.host.id.eq(userId)); // 내가 만든 모임

        if (includeStatuses != null && !includeStatuses.isEmpty()) {
            where.and(group.status.in(includeStatuses));
        }
        if (excludeStatuses != null && !excludeStatuses.isEmpty()) {
            where.and(group.status.notIn(excludeStatuses));
        }
        if (cursor != null) {
            where.and(group.id.lt(cursor));
        }

        return queryFactory
                .select(Projections.constructor(
                        MyGroupListRow.class,
                        group.id, group.title, group.status,
                        group.address.location, group.address.locationDetail,
                        group.startTime, group.endTime,
                        group.description, group.maxParticipants,
                        group.createdAt, group.updatedAt,

                        host.id, host.nickName, host.profileImage, host.profileMessage,

                        guAttend.id.count(),

                        myGu.id, myGu.groupRole, myGu.status, myGu.joinedAt, myGu.leftAt
                ))
                .from(group)
                .join(group.host, host)

                // 내 membership은 있을 수도/없을 수도 → left join + on(userId)
                .leftJoin(group.users, myGu)
                .on(myGu.user.id.eq(userId))

                // 참가자 수는 ATTEND만
                .leftJoin(group.users, guAttend)
                .on(guAttend.status.eq(GroupUserV2Status.ATTEND))

                .where(where)
                .groupBy(
                        group.id, group.title, group.status,
                        group.address.location, group.address.locationDetail,
                        group.startTime, group.endTime,
                        group.description, group.maxParticipants,
                        group.createdAt, group.updatedAt,

                        host.id, host.nickName, host.profileImage, host.profileMessage,

                        myGu.id, myGu.groupRole, myGu.status, myGu.joinedAt, myGu.leftAt
                )
                .orderBy(group.id.desc())
                .limit(limit)
                .fetch();
    }
}
