package team.wego.wegobackend.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.wego.wegobackend.common.entity.BaseTimeEntity;
import team.wego.wegobackend.common.security.Role;

@Entity
@Table(name = "users")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "password", length = 60, nullable = true)    //oauth의 경우 password == null
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "nick_name", length = 50, nullable = false, unique = true)
    private String nickName;

    @Column(name = "mbti", length = 10)
    private String mbti;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Column(name = "profile_message", length = 500)
    private String profileMessage;

    @Builder.Default
    @Column(name = "followee_count", columnDefinition = "int default 0")
    private Integer followeesCnt = 0;

    @Builder.Default
    @Column(name = "follower_count", columnDefinition = "int default 0")
    private Integer followersCnt = 0;

    @Builder.Default
    @Column(name = "group_joined_count", columnDefinition = "int default 0")
    private Integer groupJoinedCnt = 0;

    @Builder.Default
    @Column(name = "group_created_count", columnDefinition = "int default 0")
    private Integer groupCreatedCnt = 0;

    @Builder.Default
    @Column(name = "notification_enabled", nullable = false)
    private Boolean notificationEnabled = false;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "provider_id")
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private ProviderType provider;

    @Builder.Default
    @Column(name = "onboarding_completed", nullable = false)
    private Boolean onboardingCompleted = false;

    @Column(name = "current_sessionid", nullable = true)
    private String currentSessionid;

    @Builder.Default
    @OneToMany(mappedBy = "follower")
    private List<Follow> followings = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "followee")
    private List<Follow> followers = new ArrayList<>();

    public static User createLocalUser(String email, String password, String nickName, Role role) {
        return User.builder()
            .email(email)
            .password(password)
            .nickName(nickName)
            .provider(ProviderType.LOCAL)
            .role(role)
            .build();
    }

    public static User createGoogleUser(String email, String nickName, String profileImage, String providerId, Role role) {
        return User.builder()
            .email(email)
            .nickName(nickName)
            .profileImage(profileImage)
            .providerId(providerId)
            .provider(ProviderType.GOOGLE)
            .role(role)
            .build();
    }

    // ===== 카운트 증가 메서드 =====

    public void increaseFolloweeCount() {
        this.followeesCnt++;
    }

    public void decreaseFolloweeCount() {
        if (this.followeesCnt > 0) {
            this.followeesCnt--;
        }
    }

    public void increaseFollowerCount() {
        this.followersCnt++;
    }

    public void decreaseFollowerCount() {
        if (this.followersCnt > 0) {
            this.followersCnt--;
        }
    }

    public void increaseGroupJoinedCount() {
        this.groupJoinedCnt++;
    }

    public void decreaseGroupJoinedCount() {
        if (this.groupJoinedCnt > 0) {
            this.groupJoinedCnt--;
        }
    }

    public void increaseGroupCreatedCount() {
        this.groupCreatedCnt++;
    }

    public void decreaseGroupCreatedCount() {
        if (this.groupCreatedCnt > 0) {
            this.groupCreatedCnt--;
        }
    }

    public void updateProfileImage(String imageUrl) {
        this.profileImage = imageUrl;
    }

    public void updateNickName(String nickName) {
        this.nickName = nickName;
    }

    public void updateNotificationEnabled(Boolean flag) {
        this.notificationEnabled = flag;
    }

    public void updateDeleted(Boolean flag) {
        this.deleted = flag;
    }   //HARD DELETE로 변경

    public void updateMbti(String mbti) {
        this.mbti = mbti;
    }

    public void updateProfileMessage(String profileMessage) {
        this.profileMessage = profileMessage;
    }

    public void updateOnboardingCompleted() {
        this.onboardingCompleted = true;
    }

    public void updateCurrentSessionid(String sessionId) {
        this.currentSessionid = sessionId;
    }

}