package team.wego.wegobackend.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "mbti", length = 10)
    private String mbti;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @Column(name = "profile_message", length = 500)
    private String profileMessage;

    @Column(name = "notification_enabled", nullable = false)
    private Boolean notificationEnabled = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted = false;

    @Builder
    public User(String email, String password, String nickName, String phoneNumber, Role role) {
        this.email = email;
        this.password = password;
        this.nickName = nickName;
        this.phoneNumber = phoneNumber;
        this.role = role != null ? role : Role.ROLE_USER;
    }

}