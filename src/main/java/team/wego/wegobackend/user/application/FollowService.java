package team.wego.wegobackend.user.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public void follow(String followNickname, Long followerId) {
        User follower = userRepository.findById(followerId)
            .orElseThrow(UserNotFoundException::new);

        if (followNickname.equals(follower.getNickName())) {
            throw new SameFollowException();
        }

        User follow = userRepository.findByNickName(followNickname)
            .orElseThrow(UserNotFoundException::new);

        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, follow.getId())) {
            throw new ExistFollowException();
        }
    }
}