package navrat.name.vivicta.service;

import static navrat.name.vivicta.model.ProcessingStatus.PENDING;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import navrat.name.vivicta.dto.TransactionDto;
import navrat.name.vivicta.mapper.TransactionMapper;
import navrat.name.vivicta.model.Transaction;
import navrat.name.vivicta.producer.KafkaTransactionProducer;
import navrat.name.vivicta.repository.TransactionRepository;

@Service
@RequiredArgsConstructor
public class TransactionOrchestratorService {

    private final MonetaTransparentAccountService monetaService;
    private final TransactionRepository transactionRepository; // Vaše JPA repository
    private final TransactionMapper mapper; // Pokud mapujete DTO na Entity
    private final KafkaTransactionProducer kafkaProducer;

    @Transactional
    public void syncTransactions(String accountName) {
        Optional<Transaction> newestTransaction = transactionRepository.findFirstByOrderByTransactionDateDescTransactionNumberDesc();
        TransactionDto newestTransactionDto = null;

        if (newestTransaction.isPresent()) {
            newestTransactionDto = mapper.toDto(newestTransaction.get());
        }
        List<TransactionDto> incomingTransactions = monetaService.fetchAllTransactions(accountName,newestTransactionDto);
        incomingTransactions.forEach(kafkaProducer::sendTransaction);
        saveToDb(incomingTransactions);
    }

    private void saveToDb(List<TransactionDto> incomingTransactions) {
        for (TransactionDto dto : incomingTransactions) {
            Optional<Transaction> existing = transactionRepository
                    .findByTransactionNumberAndTransactionDate(dto.getTransactionNumber(), dto.getTransactionDate());

            if (existing.isEmpty()) {
                Transaction entity = mapper.toEntity(dto);
                entity.setProcessingStatus(PENDING);
                transactionRepository.save(entity);
            }
        }
    }
}
