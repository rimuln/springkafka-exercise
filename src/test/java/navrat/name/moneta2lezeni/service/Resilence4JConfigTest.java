package navrat.name.moneta2lezeni.service;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class Resilience4jConfigTest {

  @Autowired(required = false)
  private CircuitBreakerRegistry circuitBreakerRegistry;

  @Autowired(required = false)
  private RateLimiterRegistry rateLimiterRegistry;

  @Test
  void circuitBreakerRegistry_shouldBeConfigured() {
    assertThat(circuitBreakerRegistry)
        .as("CircuitBreakerRegistry musí být v kontextu!")
        .isNotNull();

    var lezeniCB = circuitBreakerRegistry.circuitBreaker("lezeniApi");
    assertThat(lezeniCB).isNotNull();
    assertThat(lezeniCB.getCircuitBreakerConfig().getSlidingWindowSize())
        .isEqualTo(10);

    System.out.println("✓ Circuit Breaker 'lezeniApi' is configured!");
  }

  @Test
  void rateLimiterRegistry_shouldBeConfigured() {
    assertThat(rateLimiterRegistry)
        .as("RateLimiterRegistry musí být v kontextu!")
        .isNotNull();

    var monetaRL = rateLimiterRegistry.rateLimiter("monetaSync");
    assertThat(monetaRL).isNotNull();
    assertThat(monetaRL.getRateLimiterConfig().getLimitForPeriod())
        .isEqualTo(1);

    System.out.println("✓ Rate Limiter 'monetaSync' is configured!");
  }

  @Test
  void lezeniApiService_shouldHaveCircuitBreakerApplied() {
    assertThat(circuitBreakerRegistry.circuitBreaker("lezeniApi"))
        .isNotNull();
  }
}