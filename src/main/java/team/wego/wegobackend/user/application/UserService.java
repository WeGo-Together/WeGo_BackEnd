package team.wego.wegobackend.user.application;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team.wego.wegobackend.image.application.dto.ImageFileResponse;
import team.wego.wegobackend.image.application.service.ImageUploadService;
import team.wego.wegobackend.image.domain.ImageFile;
import team.wego.wegobackend.user.application.dto.request.ProfileUpdateRequest;
import team.wego.wegobackend.user.application.dto.response.AvailabilityResponse;
import team.wego.wegobackend.user.application.dto.response.UserInfoResponse;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.exception.SameNicknameException;
import team.wego.wegobackend.user.exception.UserNotFoundException;
import team.wego.wegobackend.user.repository.FollowRepository;
import team.wego.wegobackend.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final ImageUploadService imageUploadService;

    @Transactional(readOnly = true)
    public UserInfoResponse getProfile(Long loginUserId, Long targetUserId) {

        User targetUser = userRepository.findById(targetUserId)
            .orElseThrow(UserNotFoundException::new);

        // 비로그인 or 본인 조회
        if (loginUserId == null || loginUserId.equals(targetUserId)) {
            return UserInfoResponse.from(targetUser);
        }

        User loginUser = userRepository.findById(loginUserId)
            .orElseThrow(UserNotFoundException::new);

        boolean isFollow = followRepository.existsByFollowerIdAndFolloweeId(loginUserId,
            targetUserId);

        return UserInfoResponse.from(targetUser, isFollow);
    }

    public UserInfoResponse updateProfileImage(Long userId, MultipartFile file) {

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        ImageFile image = imageUploadService.uploadAsWebpWithSize(file, 440, 240);

        user.updateProfileImage(image.url());

        return UserInfoResponse.from(user);
    }

    public UserInfoResponse updateProfileInfo(Long userId, ProfileUpdateRequest request) {

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (request.getNickName() != null && !request.getNickName().equals(user.getNickName())) {
            boolean isExistNickname = userRepository.existsByNickName(request.getNickName());
            if (isExistNickname) {
                throw new SameNicknameException();
            }

            user.updateNickName(request.getNickName());
        }

        if (request.getMbti() != null) {
            user.updateMbti(request.getMbti().name());
        }

        if (request.getProfileMessage() != null) {
            user.updateProfileMessage(request.getProfileMessage());
        }

        return UserInfoResponse.from(user);
    }

    public UserInfoResponse updateNotificationEnabled(Long userId, Boolean isNotificationEnabled) {

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        user.updateNotificationEnabled(isNotificationEnabled);

        return UserInfoResponse.from(user);
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse checkEmailAvailability(String email) {

        return new AvailabilityResponse(!userRepository.existsByEmail(email));
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse checkNicknameAvailability(String nickname) {

        return new AvailabilityResponse(!userRepository.existsByNickName(nickname));
    }
}
