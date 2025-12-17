package navrat.name.vivicta.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderIdGenerator {
    private static final String ORDER_KEY = "order:id";

    private final RedisTemplate<String,String> redisTemplate;

    public String generateOrderId() {
        Long id = redisTemplate.opsForValue().increment(ORDER_KEY);
        return "ORD-" + id;
    }
}
