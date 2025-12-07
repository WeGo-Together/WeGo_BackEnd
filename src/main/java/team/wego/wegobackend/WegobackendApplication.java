package team.wego.wegobackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class WegobackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(WegobackendApplication.class, args);
    }

}
