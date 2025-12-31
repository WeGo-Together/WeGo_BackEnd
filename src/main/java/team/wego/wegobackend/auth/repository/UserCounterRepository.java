package team.wego.wegobackend.auth.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import team.wego.wegobackend.auth.entity.UserCounter;

public interface UserCounterRepository extends JpaRepository<UserCounter, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT uc FROM UserCounter uc WHERE uc.id = 1")
    Optional<UserCounter> findWithLock();

}
