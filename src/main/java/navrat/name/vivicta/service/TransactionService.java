package navrat.name.vivicta.service;

import static navrat.name.vivicta.model.ProcessingStatus.IGNORE;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import navrat.name.vivicta.dto.TransactionDto;
import navrat.name.vivicta.mapper.OrderMapper;
import navrat.name.vivicta.mapper.TransactionMapper;
import navrat.name.vivicta.model.ProcessingStatus;
import navrat.name.vivicta.model.QTransaction;
import navrat.name.vivicta.model.Transaction;
import navrat.name.vivicta.producer.KafkaTransactionProducer;
import navrat.name.vivicta.repository.TransactionRepository;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository repository;
    private final KafkaTransactionProducer kafkaProducer;
    private final TransactionMapper mapper;

    public List<Transaction> findNotProcessed() {
        QTransaction transaction = QTransaction.transaction;
        return (List<Transaction>) repository.findAll(transaction.processingStatus.ne(ProcessingStatus.AUTO_PROCESSED));
    }

    public void updateTransaction(UUID transactionId, TransactionDto updateDto) {
        repository.findById(transactionId).ifPresent(entity -> {
            entity.setVariableSymbol(updateDto.getVariableSymbol());
            entity.setProcessingStatus(updateDto.getProcessingStatus());
            Transaction saved = repository.save(entity);
            if (IGNORE != updateDto.getProcessingStatus()) {
                kafkaProducer.sendTransaction(mapper.toDto(saved));
            }
        });
    }
}
