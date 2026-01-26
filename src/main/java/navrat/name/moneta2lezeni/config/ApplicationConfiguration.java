package navrat.name.moneta2lezeni.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
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
    @MonetaClient
    public RestClient monetaClient(RestClient.Builder builder,@Value("${api.moneta.base-url}") String baseUrl) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    @LezeniClient
    public RestClient lezeniClient(RestClient.Builder builder,
                                   @Value("${api.lezeni.base-url}") String baseUrl,
                                   @Value("${api.lezeni.api-token}") String apiToken) {
        return builder
                .baseUrl(baseUrl)
                .defaultHeader("X-Api-Token", apiToken)
                .build();
    }
}

