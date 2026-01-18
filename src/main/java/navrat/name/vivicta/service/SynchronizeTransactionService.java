package navrat.name.vivicta.service;

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
public class SynchronizeTransactionService {

    private final MonetaTransparentAccountService monetaService;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper mapper;
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
    }
}
