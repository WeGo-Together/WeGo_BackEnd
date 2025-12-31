package team.wego.wegobackend.user.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import team.wego.wegobackend.user.domain.ProviderType;
import team.wego.wegobackend.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNickName(String nickname);

    Optional<User> findByProviderAndProviderId(ProviderType provider, String providerId);

    boolean existsByEmail(String email);

    boolean existsByNickName(String nickname);
}
