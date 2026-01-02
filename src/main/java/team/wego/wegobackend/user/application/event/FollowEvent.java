package team.wego.wegobackend.user.application.event;

import team.wego.wegobackend.user.domain.User;

public record FollowEvent(User follower, User follow) {

}
