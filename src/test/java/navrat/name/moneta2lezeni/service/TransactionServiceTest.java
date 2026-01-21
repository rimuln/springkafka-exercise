package navrat.name.moneta2lezeni.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static navrat.name.moneta2lezeni.model.ProcessingStatus.AUTO_PROCESSED;
import static navrat.name.moneta2lezeni.model.ProcessingStatus.IGNORE;
import static navrat.name.moneta2lezeni.model.ProcessingStatus.MANUALY_FIXED;
import static navrat.name.moneta2lezeni.model.ProcessingStatus.PENDING_MANUAL;
import static navrat.name.moneta2lezeni.model.QTransaction.transaction;
import static navrat.name.moneta2lezeni.utils.DtoTestFactory.createTransactionDto;
import static navrat.name.moneta2lezeni.utils.EntityTestFactory.createTransactionEntity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import navrat.name.moneta2lezeni.mapper.TransactionMapper;
import navrat.name.moneta2lezeni.model.ProcessingStatus;
import navrat.name.moneta2lezeni.model.Transaction;
import navrat.name.moneta2lezeni.producer.KafkaTransactionProducer;
import navrat.name.moneta2lezeni.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;
    @Mock
    private TransactionMapper mapper;
    @Mock
    private KafkaTransactionProducer kafkaProducer;
    @InjectMocks
    private TransactionService serviceUnderTest;

    @Test
    void findNotProcessed_shouldReturnList_whenDataExists() {
        var expectedList = List.of(createTransactionEntity(UUID.randomUUID(), PENDING_MANUAL, 123L));
        when(repository.findAll(transaction.processingStatus.notIn(AUTO_PROCESSED, IGNORE, MANUALY_FIXED)))
                .thenReturn(expectedList);

        List<Transaction> result = serviceUnderTest.findNotProcessed();

        assertThat(result).isEqualTo(expectedList);
    }

    @Test
    void findNotProcessed_shouldReturnEmpty_whenNoDataMatch() {
        when(repository.findAll(transaction.processingStatus.notIn(AUTO_PROCESSED, IGNORE, MANUALY_FIXED)))
                .thenReturn(Collections.emptyList());

        List<Transaction> result = serviceUnderTest.findNotProcessed();

        assertThat(result).isEmpty();
    }

    @Test
    void updateTransaction_shouldSaveAndSendToKafka_whenStatusIsNotIgnore() {
        var id = UUID.randomUUID();
        var updateDto = createTransactionDto(id, PENDING_MANUAL, 12345L);
        var existingEntity = createTransactionEntity(id, ProcessingStatus.PENDING_MANUAL, null);

        var expectedSavedEntity = createTransactionEntity(id, PENDING_MANUAL, 12345L);
        var expectedKafkaDto = createTransactionDto(id, PENDING_MANUAL, 12345L);

        when(repository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(repository.save(expectedSavedEntity)).thenReturn(expectedSavedEntity);
        when(mapper.toDto(expectedSavedEntity)).thenReturn(expectedKafkaDto);

        serviceUnderTest.updateTransaction(id, updateDto);

        verify(repository).save(expectedSavedEntity);
        verify(kafkaProducer).sendTransaction(expectedKafkaDto);
        assertEquals(12345L, existingEntity.getVariableSymbol());
    }

    @Test
    void updateTransaction_shouldNotSendToKafka_whenStatusIsIgnore() {
        var id = UUID.randomUUID();
        var updateDto = createTransactionDto(id, IGNORE, 999L);
        var existingEntity = createTransactionEntity(id, PENDING_MANUAL, null);

        var expectedSavedEntity = createTransactionEntity(id, IGNORE, 999L);

        when(repository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(repository.save(expectedSavedEntity)).thenReturn(expectedSavedEntity);

        serviceUnderTest.updateTransaction(id, updateDto);

        verify(repository).save(expectedSavedEntity);
        verifyNoInteractions(kafkaProducer);
    }
}
