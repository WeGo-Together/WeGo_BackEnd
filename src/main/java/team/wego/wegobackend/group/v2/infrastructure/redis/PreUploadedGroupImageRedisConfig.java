package team.wego.wegobackend.group.v2.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import team.wego.wegobackend.group.v2.application.dto.common.PreUploadedGroupImage;

@Configuration
public class PreUploadedGroupImageRedisConfig {

    @Bean
    public RedisTemplate<String, PreUploadedGroupImage> preUploadedGroupImageRedisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper
    ) {
        RedisTemplate<String, PreUploadedGroupImage> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // key: String
        StringRedisSerializer keySerializer = new StringRedisSerializer();

        // value: JSON (Jackson)
        Jackson2JsonRedisSerializer<PreUploadedGroupImage> valueSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, PreUploadedGroupImage.class);

        template.setKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);

        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }
}