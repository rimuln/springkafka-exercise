package navrat.name.moneta2lezeni.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import navrat.name.moneta2lezeni.dto.TransactionDto;
import navrat.name.moneta2lezeni.mapper.TransactionMapper;
import navrat.name.moneta2lezeni.model.Transaction;
import navrat.name.moneta2lezeni.producer.KafkaTransactionProducer;
import navrat.name.moneta2lezeni.repository.TransactionRepository;

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
