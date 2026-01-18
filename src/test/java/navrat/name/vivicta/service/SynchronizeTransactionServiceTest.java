package navrat.name.vivicta.service;

import navrat.name.vivicta.dto.TransactionDto;
import navrat.name.vivicta.mapper.TransactionMapper;
import navrat.name.vivicta.model.Transaction;
import navrat.name.vivicta.producer.KafkaTransactionProducer;
import navrat.name.vivicta.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static navrat.name.vivicta.utils.DtoTestFactory.createTransactionDto;
import static navrat.name.vivicta.utils.EntityTestFactory.createTransactionEntity;

@ExtendWith(MockitoExtension.class)
class SynchronizeTransactionServiceTest {

    @Mock
    private MonetaTransparentAccountService monetaService;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TransactionMapper mapper;
    @Mock
    private KafkaTransactionProducer kafkaProducer;

    @InjectMocks
    private SynchronizeTransactionService serviceUnderTest;

    @Test
    void syncTransactions_shouldFetchAndSendAllTransactions() {
        String accountName = "246594777";
        UUID id = UUID.randomUUID();
        Transaction newestEntity = createTransactionEntity(id, null, null);
        TransactionDto newestDto = createTransactionDto(id, null, null);

        TransactionDto incomingDto = createTransactionDto(UUID.randomUUID(), null, null);
        List<TransactionDto> incomingTransactions = List.of(incomingDto);

        when(transactionRepository.findFirstByOrderByTransactionDateDescTransactionNumberDesc())
                .thenReturn(Optional.of(newestEntity));
        when(mapper.toDto(newestEntity)).thenReturn(newestDto);
        when(monetaService.fetchAllTransactions(accountName, newestDto))
                .thenReturn(incomingTransactions);

        serviceUnderTest.syncTransactions(accountName);

        verify(kafkaProducer).sendTransaction(incomingDto);
    }

    @Test
    void syncTransactions_shouldWorkWithoutNewestTransaction() {
        String accountName = "246594777";
        TransactionDto incomingDto = createTransactionDto(UUID.randomUUID(), null, null);
        List<TransactionDto> incomingTransactions = List.of(incomingDto);

        when(transactionRepository.findFirstByOrderByTransactionDateDescTransactionNumberDesc())
                .thenReturn(Optional.empty());
        when(monetaService.fetchAllTransactions(accountName, null))
                .thenReturn(incomingTransactions);

        serviceUnderTest.syncTransactions(accountName);

        verify(kafkaProducer).sendTransaction(incomingDto);
    }
}