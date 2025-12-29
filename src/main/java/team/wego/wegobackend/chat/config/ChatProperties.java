package team.wego.wegobackend.chat.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "chat")
@Getter
@Setter
public class ChatProperties {

    private AutoJoin autoJoin = new AutoJoin();
    private Dm dm = new Dm();
    private Websocket websocket = new Websocket();
    private Message message = new Message();
    private Batch batch = new Batch();

    @Getter
    @Setter
    public static class AutoJoin {
        private boolean enabled = true;
    }

    @Getter
    @Setter
    public static class Dm {
        private DeletePolicy deletePolicy = DeletePolicy.NONE;
        private int inactiveDays = 90;
    }

    public enum DeletePolicy {
        NONE,
        INACTIVE_DAYS
    }

    @Getter
    @Setter
    public static class Websocket {
        private String endpoint = "/ws-chat";
        private String[] allowedOrigins = {"http://localhost:3000"};
    }

    @Getter
    @Setter
    public static class Message {
        private int maxLength = 1000;
    }

    @Getter
    @Setter
    public static class Batch {
        private DeleteExpiredChat deleteExpiredChat = new DeleteExpiredChat();
    }

    @Getter
    @Setter
    public static class DeleteExpiredChat {
        private String cron = "0 0 * * * *";
    }
}
