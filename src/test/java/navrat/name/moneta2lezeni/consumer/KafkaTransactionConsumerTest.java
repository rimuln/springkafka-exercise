package navrat.name.moneta2lezeni.consumer;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static navrat.name.moneta2lezeni.model.ProcessingStatus.AUTO_FAILED;
import static navrat.name.moneta2lezeni.model.ProcessingStatus.AUTO_PROCESSED;
import static navrat.name.moneta2lezeni.model.ProcessingStatus.MANUALY_FIXED;
import static navrat.name.moneta2lezeni.model.ProcessingStatus.PENDING_MANUAL;
import static navrat.name.moneta2lezeni.utils.DtoTestFactory.createTransactionDto;
import static navrat.name.moneta2lezeni.utils.EntityTestFactory.createTransactionEntity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import navrat.name.moneta2lezeni.mapper.TransactionMapper;
import navrat.name.moneta2lezeni.model.ProcessingStatus;
import navrat.name.moneta2lezeni.repository.TransactionRepository;
import navrat.name.moneta2lezeni.utils.DtoTestFactory;

@ExtendWith(MockitoExtension.class)
class KafkaTransactionConsumerTest {

    @Mock
    private RestClient restClient;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TransactionMapper mapper;
    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private KafkaTransactionConsumer consumerUnderTest;

    private static Stream<Arguments> provideTransactionsForAutoProcessing() {
        return Stream.of(
                Arguments.of(BigDecimal.valueOf(100), 15000L, AUTO_PROCESSED),
                Arguments.of(BigDecimal.valueOf(40), 15000L, PENDING_MANUAL),
                Arguments.of(BigDecimal.valueOf(100), 5000L, PENDING_MANUAL),
                Arguments.of(null, 15000L, PENDING_MANUAL)
        );
    }

    @ParameterizedTest
    @MethodSource("provideTransactionsForAutoProcessing")
    void consume_shouldApplyCorrectStatusBasedOnAutoProcessingEligibility(BigDecimal amount, Long vs, ProcessingStatus expectedStatus) {
        var dto = createTransactionDto(null, null, vs);
        dto.setAmount(amount);
        dto.setTransactionNumber(1);
        dto.setTransactionDate(DtoTestFactory.TEST_DATE);

        var entity = createTransactionEntity(null, null, vs);
        entity.setAmount(amount);

        when(transactionRepository.findByTransactionNumberAndTransactionDate(anyInt(), any())).thenReturn(Optional.of(entity));

        if (expectedStatus == AUTO_PROCESSED) {
            mockRestClientSuccess();
        }

        consumerUnderTest.consume(dto);

        verify(transactionRepository).save(argThat(t -> t.getProcessingStatus() == expectedStatus));
    }

    @Test
    void consume_shouldForceApiCall_whenStatusIsPendingManual() {
        var dto = createTransactionDto(UUID.randomUUID(), PENDING_MANUAL, 500L);
        dto.setAmount(BigDecimal.valueOf(10));

        var entity = createTransactionEntity(dto.getId(), PENDING_MANUAL, 500L);
        entity.setAmount(BigDecimal.valueOf(10));

        when(transactionRepository.findByTransactionNumberAndTransactionDate(anyInt(), any())).thenReturn(Optional.of(entity));
        mockRestClientSuccess();

        consumerUnderTest.consume(dto);

        verify(transactionRepository).save(argThat(t -> t.getProcessingStatus() == MANUALY_FIXED));
    }

    @Test
    void consume_shouldSetAutoFailed_whenApiCallFails() {
        var dto = createTransactionDto(null, null, 20000L);
        dto.setAmount(BigDecimal.valueOf(100));
        var entity = createTransactionEntity(null, null, 20000L);
        entity.setAmount(BigDecimal.valueOf(100));

        when(transactionRepository.findByTransactionNumberAndTransactionDate(anyInt(), any())).thenReturn(Optional.of(entity));

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(String.class)).thenThrow(new RuntimeException("API Down"));

        consumerUnderTest.consume(dto);

        verify(transactionRepository).save(argThat(t -> t.getProcessingStatus() == AUTO_FAILED));
    }

    @Test
    void consume_shouldSetAutoFailed_whenApiStatusIsNot2xx() {
        var dto = createTransactionDto(null, null, 20000L);
        dto.setAmount(BigDecimal.valueOf(100));
        var entity = createTransactionEntity(null, null, 20000L);
        entity.setAmount(BigDecimal.valueOf(100));

        when(transactionRepository.findByTransactionNumberAndTransactionDate(anyInt(), any())).thenReturn(Optional.of(entity));

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(String.class)).thenReturn(ResponseEntity.notFound().build());

        consumerUnderTest.consume(dto);

        verify(transactionRepository).save(argThat(t -> t.getProcessingStatus() == AUTO_FAILED));
    }

    private void mockRestClientSuccess() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(String.class)).thenReturn(ResponseEntity.ok("Success"));
    }
}