package team.wego.wegobackend.group;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.common.security.CustomUserDetails;
import team.wego.wegobackend.common.security.Role;
import team.wego.wegobackend.group.application.dto.v1.request.CreateGroupImageRequest;
import team.wego.wegobackend.group.application.dto.v1.request.CreateGroupRequest;
import team.wego.wegobackend.group.application.service.v1.GroupService;
import team.wego.wegobackend.group.domain.repository.v1.GroupRepository;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.repository.UserRepository;

@DisplayName("모임 커서 검색 더미 데이터 생성 테스트")
@SpringBootTest
class GroupCursorDummyTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupService groupService;

    private CustomUserDetails toUserDetails(User user) {
        return new CustomUserDetails(user);
    }

    @Test
    @Transactional
    @Rollback(false)
    @DisplayName("회원 여러 명 + 모임 여러 개 생성해서 커서 기반 조회를 실험하기 위한 더미 데이터 생성용 테스트")
    void createDummyUsersAndGroupsForCursorTest() {

        // 이미 어느 정도 모임이 있다면 굳이 또 넣지 않게 가드 하나
        if (groupRepository.count() >= 50) {
            return;
        }

        // 1. 공통 호스트 한 명 생성
        User host = userRepository.findByEmail("cursor-host@example.com")
                .orElseGet(() -> userRepository.save(
                        new User(
                                "cursor-host@example.com",
                                "Test1234!@#",
                                "CursorHost",
                                Role.ROLE_USER
                        )
                ));

        // 2. 회원 N명 생성 (예: 5명)
        int userCount = 5;
        int groupsPerUser = 3;   // 회원 한 명당 3개의 모임

        IntStream.rangeClosed(1, userCount).forEach(uIndex -> {
            String email = "cursor-member" + uIndex + "@example.com";

            // 이미 있으면 재사용, 없으면 새로 생성
            User member = userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.save(
                            new User(
                                    email,
                                    "Test1234!@#",
                                    "CursorMember" + uIndex,
                                    Role.ROLE_USER
                            )
                    ));

            // service 레벨을 그대로 쓰기 위해 User -> CustomUserDetails 로 감싸기
            CustomUserDetails memberDetails = toUserDetails(member);

            // 3. 각 회원마다 groupsPerUser만큼 모임 생성
            IntStream.rangeClosed(1, groupsPerUser).forEach(gIndex -> {
                int seq = (uIndex - 1) * groupsPerUser + gIndex; // 전체 시퀀스 번호 (1,2,3,...)

                LocalDateTime start = LocalDateTime.of(2026, 12, 10, 19, 0)
                        .plusDays(seq);
                LocalDateTime end = start.plusHours(2);

                // 태그 (검색용)
                List<String> tags = List.of(
                        "스터디",
                        "더미" + seq
                );

                // 이미지 1세트 (440 / 100) - sortOrder 0만 사용
                String base =
                        "https://cdn.example.com/groups/dummy/member_" + uIndex + "_group_" + gIndex
                                + "_img0";
                List<CreateGroupImageRequest> images = List.of(
                        new CreateGroupImageRequest(
                                0,                                  // sortOrder
                                base + "_440x240.webp",             // imageUrl440x240
                                base + "_100x100.webp"              // imageUrl100x100
                        )
                );

                // CreateGroupRequest 생성
                CreateGroupRequest request = new CreateGroupRequest(
                        "커서 테스트 모임 " + seq + " (member " + uIndex + ")",
                        "서울 강남구",
                        "강남역 " + seq + "번 출구 근처 카페",
                        start,
                        end,
                        tags,
                        "커서 기반 페이징 테스트용 더미 모임입니다. 번호: " + seq,
                        10,
                        images
                );

                groupService.createGroup(memberDetails, request);
            });

        });
    }
}
