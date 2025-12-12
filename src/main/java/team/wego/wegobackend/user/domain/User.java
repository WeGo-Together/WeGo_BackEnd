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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.wego.wegobackend.common.entity.BaseTimeEntity;
import team.wego.wegobackend.common.security.Role;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "password", length = 60, nullable = false)
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

    @Column(name = "followee_count", columnDefinition = "int default 0")
    private Integer followeesCnt = 0;

    @Column(name = "follower_count", columnDefinition = "int default 0")
    private Integer followersCnt = 0;

    @Column(name = "group_joined_count", columnDefinition = "int default 0")
    private Integer groupJoinedCnt = 0;

    @Column(name = "group_created_count", columnDefinition = "int default 0")
    private Integer groupCreatedCnt = 0;

    @Column(name = "notification_enabled", nullable = false)
    private Boolean notificationEnabled = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted = false;

    @OneToMany(mappedBy = "follower")
    private List<Follow> followings = new ArrayList<>();

    @OneToMany(mappedBy = "followee")
    private List<Follow> followers = new ArrayList<>();


    @Builder
    public User(String email, String password, String nickName, Role role) {
        this.email = email;
        this.password = password;
        this.nickName = nickName;
        this.role = role != null ? role : Role.ROLE_USER;
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

    public void updatedeleted(Boolean flag) {
        this.deleted = flag;
    }

    public void updateMbti(String mbti) {
        this.mbti = mbti;
    }

    public void updateProfileMessage(String profileMessage) {
        this.profileMessage = profileMessage;
    }

}