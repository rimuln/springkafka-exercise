package navrat.name.vivicta.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestClient;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    @MonetaClient
    public RestClient monetaClient(RestClient.Builder builder) {
        return builder
                .baseUrl("https://transparentniucty.moneta.cz/api/v1")
                .build();
    }

    @Bean
    @LezeniClient
    public RestClient lezeniClient(RestClient.Builder builder) {
        return builder
                .baseUrl("https://lezeni.navrat.name/api/rest/prezence/platba")
                .build();
    }
}

