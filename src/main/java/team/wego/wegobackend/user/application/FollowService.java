package team.wego.wegobackend.user.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.user.domain.Follow;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.exception.ExistFollowException;
import team.wego.wegobackend.user.exception.SameFollowException;
import team.wego.wegobackend.user.exception.UserNotFoundException;
import team.wego.wegobackend.user.repository.FollowRepository;
import team.wego.wegobackend.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FollowService {

    private final FollowRepository followRepository;

    private final UserRepository userRepository;

    public void follow(Long followId, Long followerId) {
        if(followerId.equals(followId)) {
            throw new SameFollowException();
        }

        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followId)) {
            throw new ExistFollowException();
        }

        User follower = userRepository.findById(followerId)
            .orElseThrow(UserNotFoundException::new);

        User follow = userRepository.findById(followId)
            .orElseThrow(UserNotFoundException::new);

        followRepository.save(Follow.builder()
            .follower(follower)
            .follow(follow)
            .build());
    }
}
