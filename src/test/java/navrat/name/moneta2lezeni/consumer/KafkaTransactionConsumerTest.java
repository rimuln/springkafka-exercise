package navrat.name.moneta2lezeni.consumer;

import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static navrat.name.moneta2lezeni.model.ProcessingStatus.AUTO_PROCESSED;
import static navrat.name.moneta2lezeni.model.ProcessingStatus.MANUALY_FIXED;
import static navrat.name.moneta2lezeni.model.ProcessingStatus.PENDING_MANUAL;
import static navrat.name.moneta2lezeni.utils.DtoTestFactory.TEST_DATE;
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

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import navrat.name.moneta2lezeni.mapper.TransactionMapper;
import navrat.name.moneta2lezeni.model.ProcessingStatus;
import navrat.name.moneta2lezeni.repository.TransactionRepository;
import navrat.name.moneta2lezeni.service.LezeniApiService;
import navrat.name.moneta2lezeni.utils.EntityTestFactory;

@ExtendWith(MockitoExtension.class)
class KafkaTransactionConsumerTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TransactionMapper mapper;
    @Mock
    private LezeniApiService lezeniApiService;

    @InjectMocks
    private KafkaTransactionConsumer consumerUnderTest;

    private static Stream<Arguments> provideTransactionsForAutoProcessing() {
        return Stream.of(
                Arguments.of(BigDecimal.valueOf(500), 15000L, AUTO_PROCESSED),
                Arguments.of(BigDecimal.valueOf(40), 15000L, PENDING_MANUAL),
                Arguments.of(BigDecimal.valueOf(100), 5000L, PENDING_MANUAL),
                Arguments.of(null, 15000L, PENDING_MANUAL),
                Arguments.of(BigDecimal.valueOf(6500), 150000L, PENDING_MANUAL)
        );
    }

    @ParameterizedTest
    @MethodSource("provideTransactionsForAutoProcessing")
    void consume_shouldApplyCorrectStatusBasedOnAutoProcessingEligibility(BigDecimal amount, Long vs, ProcessingStatus expectedStatus) {
        var dto = createTransactionDto(null, null, vs);
        dto.setAmount(amount);
        dto.setTransactionNumber(1);
        dto.setTransactionSentDate(TEST_DATE);

        var entity = createTransactionEntity(null, null, vs);
        entity.setAmount(amount);

        when(transactionRepository.findByTransactionNumberAndTransactionSentDate(1, TEST_DATE)).thenReturn(Optional.of(entity));

        if (expectedStatus == AUTO_PROCESSED) {
            when(lezeniApiService.callExternalApi(vs, amount, AUTO_PROCESSED)).thenReturn(AUTO_PROCESSED);
        }

        consumerUnderTest.consume(dto);

        verify(transactionRepository).save(argThat(t -> t.getProcessingStatus() == expectedStatus));
    }

    @Test
    void consume_shouldForceApiCall_whenStatusIsPendingManual() {
        var vs = 500L;
        var amount = BigDecimal.valueOf(10);
        var id = UUID.randomUUID();
        var dto = createTransactionDto(id, PENDING_MANUAL, vs);
        dto.setAmount(amount);
        dto.setTransactionNumber(1);
        dto.setTransactionSentDate(TEST_DATE);

        var entity = createTransactionEntity(id, PENDING_MANUAL, vs);
        entity.setAmount(amount);

        when(transactionRepository.findByTransactionNumberAndTransactionSentDate(1, EntityTestFactory.TEST_DATE)).thenReturn(Optional.of(entity));
        when(lezeniApiService.callExternalApi(vs, amount, MANUALY_FIXED)).thenReturn(MANUALY_FIXED);

        consumerUnderTest.consume(dto);

        verify(transactionRepository).save(argThat(t -> t.getProcessingStatus() == MANUALY_FIXED));
    }
}
