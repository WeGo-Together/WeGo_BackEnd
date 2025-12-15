package team.wego.wegobackend.group;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.common.security.CustomUserDetails;
import team.wego.wegobackend.common.security.Role;
import team.wego.wegobackend.group.application.dto.v1.request.CreateGroupRequest;
import team.wego.wegobackend.group.application.service.v1.GroupService;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.repository.UserRepository;

@DisplayName("내 모임 목록 조회 더미 테스트")
@SpringBootTest
class MyGroupsDummyTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupService groupService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final String RAW_PASSWORD = "Test1234!@#";

    private User getOrCreateUser(String email, String nickname) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    String encodedPw = passwordEncoder.encode(RAW_PASSWORD);
                    return userRepository.save(
                            new User(
                                    email,
                                    encodedPw,
                                    nickname,
                                    Role.ROLE_USER
                            )
                    );
                });
    }

    private CustomUserDetails toUserDetails(User user) {
        return new CustomUserDetails(user);
    }

    @Test
    @Transactional
    @Rollback(false)
    @DisplayName("CURRENT / PAST / MY_POST 타입을 모두 만족하는 더미 모임 15개 생성")
    void createDummyForMyGroups() {
        // 1. HOST / MEMBER1 준비
        User host = getOrCreateUser("test@example.com", "Beemo");
        User member1 = getOrCreateUser("test1@example.com", "Heemo");

        CustomUserDetails hostDetails = toUserDetails(host);
        CustomUserDetails memberDetails = toUserDetails(member1);

        LocalDateTime now = LocalDateTime.now();

        // 2. 총 15개 모임 생성
        //   - 1~8번: endTime 이 미래 → CURRENT
        //   - 9~15번: endTime 이 과거 → PAST
        int totalGroups = 15;

        for (int i = 1; i <= totalGroups; i++) {

            boolean isCurrent = (i <= 8);

            LocalDateTime start;
            LocalDateTime end;

            if (isCurrent) {
                start = now.plusDays(i);
                end = start.plusHours(2);
            } else {
                start = now.minusDays(i);
                end = start.plusHours(2);
            }

            String titlePrefix = isCurrent ? "더미 CURRENT 모임" : "더미 PAST 모임";

            CreateGroupRequest request = new CreateGroupRequest(
                    titlePrefix + " #" + i,
                    "서울 강남구",
                    (isCurrent ? "강남역 CURRENT 카페 " : "강남역 PAST 카페 ") + i + "번 출구 근처",
                    start,
                    end,
                    List.of(
                            "더미",
                            isCurrent ? "current" : "past",
                            "seq-" + i
                    ),
                    "내 모임(" + (isCurrent ? "current" : "past") + ") 더미 데이터, seq=" + i,
                    10,
                    List.of()
            );

            Long groupId = groupService.createGroup(hostDetails, request).id();

            // MEMBER1도 모두 참여
            groupService.attendGroup(memberDetails, groupId);
        }
    }
}
