package team.wego.wegobackend.user.application;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.wego.wegobackend.user.application.dto.response.FollowListResponse;
import team.wego.wegobackend.user.application.dto.response.FollowResponse;
import team.wego.wegobackend.user.application.dto.response.FollowerListResponse;
import team.wego.wegobackend.user.application.dto.response.WrapperFollowerResponse;
import team.wego.wegobackend.user.application.event.FollowEvent;
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

    private final ApplicationEventPublisher eventPublisher;

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

        // 알림 이벤트 발행
        eventPublisher.publishEvent(new FollowEvent(follower, follow));
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

    @Transactional(readOnly = true)
    public FollowListResponse followList(Long userId, Long cursor, Integer size) {

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException();
        }

        List<FollowResponse> list = followRepository.findFollowingList(userId, cursor, size);

        Long nextCursor = list.isEmpty() ? null : list.getLast().getFollowId();

        return new FollowListResponse(list, nextCursor);
    }

    @Transactional(readOnly = true)
    public FollowerListResponse followerList(Long userId, Long cursor, Integer size) {

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException();
        }

        List<FollowResponse> list = followRepository.findFollowerList(userId, cursor, size);

        // 맞팔로우 여부 조회: 내가 팔로우한 사람들의 ID 조회
        Set<Long> followingUserIds = followRepository.findFolloweeIdsByFollowerId(userId);

        List<WrapperFollowerResponse> result = new ArrayList<>();

        for(FollowResponse follower : list) {
            boolean isFollow = followingUserIds.contains(follower.getUserId());
            WrapperFollowerResponse response = new WrapperFollowerResponse(follower, isFollow);
            result.add(response);
        }

        Long nextCursor = list.isEmpty() ? null : list.getLast().getFollowId();

        return new FollowerListResponse(result, nextCursor);
    }

}
