package navrat.name.moneta2lezeni.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static navrat.name.moneta2lezeni.model.ProcessingStatus.AUTO_FAILED;
import static navrat.name.moneta2lezeni.model.ProcessingStatus.AUTO_PROCESSED;
import static navrat.name.moneta2lezeni.model.ProcessingStatus.PENDING;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import navrat.name.moneta2lezeni.model.ProcessingStatus;

@ExtendWith(MockitoExtension.class)
class LezeniApiServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private LezeniApiService serviceUnderTest;

    @BeforeEach
    void setUp() {
        String endpoint = "http://test-api/{vs}/{amount}";
        serviceUnderTest = new LezeniApiService(restClient, endpoint);
    }

    @Test
    void callExternalApi_shouldReturnSuccessStatus_whenApiCallIsSuccessful() {
        var vs = 12345L;
        var amount = BigDecimal.valueOf(100);
        
        mockRestClientChain();
        when(responseSpec.toEntity(String.class)).thenReturn(ResponseEntity.ok("Success"));

        ProcessingStatus result = serviceUnderTest.callExternalApi(vs, amount, AUTO_PROCESSED);

        assertThat(result).isEqualTo(AUTO_PROCESSED);
    }

    @Test
    void callExternalApi_shouldReturnAutoFailed_whenApiStatusIsNot2xx() {
        var vs = 12345L;
        var amount = BigDecimal.valueOf(100);

        mockRestClientChain();
        when(responseSpec.toEntity(String.class)).thenReturn(ResponseEntity.notFound().build());

        ProcessingStatus result = serviceUnderTest.callExternalApi(vs, amount, AUTO_PROCESSED);

        assertThat(result).isEqualTo(AUTO_FAILED);
    }

    @Test
    void apiFallback_shouldReturnPending() {
        ProcessingStatus result = serviceUnderTest.apiFallback(12345L, BigDecimal.valueOf(100), AUTO_PROCESSED, new RuntimeException("Error"));
        assertThat(result).isEqualTo(PENDING);
    }

    private void mockRestClientChain() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(), any())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    }
}
