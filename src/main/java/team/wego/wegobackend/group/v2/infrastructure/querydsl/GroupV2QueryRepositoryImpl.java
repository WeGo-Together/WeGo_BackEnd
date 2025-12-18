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
                        Collectors.mapping(tp -> tp.get(tag.name),
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
}
