package team.wego.wegobackend.user.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.user.application.dto.response.FollowListResponse;
import team.wego.wegobackend.user.application.dto.response.FollowResponse;
import team.wego.wegobackend.user.domain.Follow;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.exception.ExistFollowException;
import team.wego.wegobackend.user.exception.NotFoundFollowException;
import team.wego.wegobackend.user.exception.SameFollowException;
import team.wego.wegobackend.user.exception.SameUnFollowException;
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

        followRepository.save(Follow.builder()
            .follower(follower)
            .follow(follow)
            .build());

        follower.increaseFolloweeCount();
        follow.increaseFollowerCount();
    }

    public void unFollow(String unFollowNickname, Long followerId) {
        User follower = userRepository.findById(followerId)
            .orElseThrow(UserNotFoundException::new);

        if (unFollowNickname.equals(follower.getNickName())) {
            throw new SameUnFollowException();
        }

        User follow = userRepository.findByNickName(unFollowNickname)
            .orElseThrow(UserNotFoundException::new);

        Follow followEntity = followRepository.findByFollowerIdAndFolloweeId(followerId,
                follow.getId())
            .orElseThrow(NotFoundFollowException::new);

        followRepository.delete(followEntity);

        follower.decreaseFolloweeCount();
        follow.decreaseFollowerCount();
    }

    public FollowListResponse followList(Long userId, Long cursor, Integer size) {

        if(!userRepository.existsById(userId)) {
            throw new UserNotFoundException();
        }

        List<FollowResponse> list = followRepository.findFollowingList(userId, cursor, size);

        Long nextCursor = list.isEmpty() ? null : list.getLast().getFollowId();

        return new FollowListResponse(list, nextCursor);
    }
}