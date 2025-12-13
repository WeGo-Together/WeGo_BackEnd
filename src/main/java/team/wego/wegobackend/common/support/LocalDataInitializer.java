package team.wego.wegobackend.common.support;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import team.wego.wegobackend.common.security.Role;
import team.wego.wegobackend.user.domain.Follow;
import team.wego.wegobackend.user.domain.User;

/**
 * local 테스트 데이터 초기화를 위한 설정
 * */
@Configuration
@Profile("local")
public class LocalDataInitializer implements ApplicationRunner {

    private final EntityManager em;

    public LocalDataInitializer(EntityManager em) {
        this.em = em;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        // test user ~100
        for(long i = 0; i <= 100; i++) {
            em.persist(User.builder()
                .email("user" + i + "@test.com")
                .password("1q2w3e4r!")
                .nickName("user" + i)
                .role(Role.ROLE_USER)
                .build()
            );
        }

        // user1 → user2~50
        for (long i = 2; i <= 50; i++) {
            em.persist(new Follow(
                em.getReference(User.class, 1L),
                em.getReference(User.class, i)
            ));
        }
    }
}
