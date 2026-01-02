package team.wego.wegobackend.group.v2.infrastructure.querydsl;

import static team.wego.wegobackend.group.v2.domain.entity.QGroupImageV2.groupImageV2;
import static team.wego.wegobackend.group.v2.domain.entity.QGroupImageV2Variant.groupImageV2Variant;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import team.wego.wegobackend.group.v2.domain.entity.GroupImageV2VariantType;
import team.wego.wegobackend.group.v2.domain.repository.GroupImageV2QueryRepository;

@RequiredArgsConstructor
@Repository
public class GroupImageV2QueryRepositoryImpl implements GroupImageV2QueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public String fetchThumbnail100Url(Long groupId) {
        return queryFactory
                .select(groupImageV2Variant.imageUrl)
                .from(groupImageV2)
                .join(groupImageV2.variants, groupImageV2Variant)
                .where(
                        groupImageV2.group.id.eq(groupId),
                        groupImageV2Variant.type.eq(GroupImageV2VariantType.THUMBNAIL_100_100)
                )
                .orderBy(groupImageV2.sortOrder.asc())
                .fetchFirst(); // 없으면 null
    }
}