package team.wego.wegobackend.user.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team.wego.wegobackend.image.application.dto.ImageFileResponse;
import team.wego.wegobackend.image.application.service.ImageUploadService;
import team.wego.wegobackend.image.domain.ImageFile;
import team.wego.wegobackend.user.application.dto.request.ProfileUpdateRequest;
import team.wego.wegobackend.user.application.dto.response.UserInfoResponse;
import team.wego.wegobackend.user.domain.User;
import team.wego.wegobackend.user.exception.UserNotFoundException;
import team.wego.wegobackend.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ImageUploadService imageUploadService;

    @Transactional(readOnly = true)
    public UserInfoResponse getProfile(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        return UserInfoResponse.from(user);
    }

    public ImageFileResponse updateProfileImage(Long userId, MultipartFile file) {

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        ImageFile image = imageUploadService.uploadAsWebpWithSize(file, 440, 240);

        user.updateProfileImage(image.url());

        return ImageFileResponse.from(image);
    }

    public UserInfoResponse updateProfileInfo(Long userId, ProfileUpdateRequest request) {

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (request.getNickName() != null) {
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
}
