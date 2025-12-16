package team.wego.wegobackend.group.infrastructure.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import team.wego.wegobackend.group.domain.entity.GroupUserStatus;
import team.wego.wegobackend.group.domain.entity.QGroup;
import team.wego.wegobackend.group.domain.entity.QGroupImage;
import team.wego.wegobackend.group.domain.entity.QGroupTag;
import team.wego.wegobackend.group.domain.entity.QGroupUser;
import team.wego.wegobackend.group.domain.repository.v2.GroupV2QueryRepository;
import team.wego.wegobackend.group.infrastructure.querydsl.projection.GroupListRow;
import team.wego.wegobackend.tag.domain.entity.QTag;
import team.wego.wegobackend.user.domain.QUser;


@RequiredArgsConstructor
@Repository
public class GroupV2QueryRepositoryImpl implements GroupV2QueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final String MAIN_TOKEN = "440x240";

    @Override
    public List<GroupListRow> fetchGroupRows(String keyword, Long cursor, int limit) {
        QGroup group = QGroup.group;
        QUser user = QUser.user;
        QGroupUser groupUser = QGroupUser.groupUser;

        BooleanBuilder where = new BooleanBuilder();
        where.and(group.deletedAt.isNull());

        if (keyword != null && !keyword.trim().isBlank()) {
            String key = keyword.trim();
            where.and(
                    group.title.containsIgnoreCase(key)
                            .or(group.location.containsIgnoreCase(key))
                            .or(group.description.containsIgnoreCase(key))
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
                        group.location,
                        group.locationDetail,
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
                .leftJoin(group.users, groupUser).on(groupUser.status.eq(GroupUserStatus.ATTEND))
                .where(where)
                .groupBy(
                        group.id,
                        group.title,
                        group.location,
                        group.locationDetail,
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

        QGroupTag gt = QGroupTag.groupTag;
        QTag t = QTag.tag;

        var tuples = queryFactory
                .select(gt.group.id, t.name)
                .from(gt)
                .join(gt.tag, t)
                .where(gt.group.id.in(groupIds))
                .fetch();

        return tuples.stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(gt.group.id),
                        Collectors.mapping(tp -> tp.get(t.name), Collectors.toList())
                ));
    }

    @Override
    public Map<Long, List<String>> fetchMainImageUrlsByGroupIds(List<Long> groupIds,
            int perGroupLimit) {
        if (groupIds == null || groupIds.isEmpty()) {
            return Map.of();
        }

        QGroupImage gi = QGroupImage.groupImage;

        List<Tuple> tuples = queryFactory
                .select(gi.group.id, gi.sortOrder, gi.imageUrl)
                .from(gi)
                .where(
                        gi.group.id.in(groupIds),
                        gi.imageUrl.contains(MAIN_TOKEN)
                )
                .orderBy(gi.group.id.desc(), gi.sortOrder.asc(), gi.id.asc())
                .fetch();

        Map<Long, List<String>> result = new java.util.HashMap<>();

        for (Tuple tuple : tuples) {
            Long groupId = tuple.get(gi.group.id);
            String url = tuple.get(gi.imageUrl);
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
