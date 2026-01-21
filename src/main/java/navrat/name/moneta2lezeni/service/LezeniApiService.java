package navrat.name.moneta2lezeni.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import navrat.name.moneta2lezeni.config.LezeniClient;
import navrat.name.moneta2lezeni.model.ProcessingStatus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import static navrat.name.moneta2lezeni.model.ProcessingStatus.AUTO_FAILED;
import static navrat.name.moneta2lezeni.model.ProcessingStatus.PENDING;

import java.math.BigDecimal;

@Slf4j
@Service
public class LezeniApiService {

    private final RestClient restClient;
    private final String endpointPlatba;

    public LezeniApiService(@LezeniClient RestClient restClient, @Value("${api.lezeni.endpoint-platba}") String endpointPlatba) {
        this.restClient = restClient;
        this.endpointPlatba = endpointPlatba;
    }

    @CircuitBreaker(name = "lezeniApi", fallbackMethod = "apiFallback")
    public ProcessingStatus callExternalApi(Long variableSymbol, BigDecimal amount, ProcessingStatus successStatus) {
        var response = restClient.get()
                .uri(endpointPlatba, variableSymbol, amount)
                .retrieve()
                .toEntity(String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("API call successful for VS {}", variableSymbol);
            return successStatus;
        } else {
            log.warn("API returned error {} for VS {}", response.getStatusCode(), variableSymbol);
            return AUTO_FAILED;
        }
    }

    public ProcessingStatus apiFallback(Long variableSymbol, BigDecimal amount, ProcessingStatus successStatus, Throwable t) {
        log.error("Circuit Breaker OPEN or API failed for VS {}. Reason: {}", variableSymbol, t.getMessage());
        return PENDING;
    }
}