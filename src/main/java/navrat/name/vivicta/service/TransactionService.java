package navrat.name.vivicta.service;

import static navrat.name.vivicta.model.ProcessingStatus.AUTO_PROCESSED;
import static navrat.name.vivicta.model.ProcessingStatus.IGNORE;
import static navrat.name.vivicta.model.ProcessingStatus.MANUALY_FIXED;
import static navrat.name.vivicta.model.QTransaction.transaction;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import navrat.name.vivicta.dto.TransactionDto;
import navrat.name.vivicta.mapper.TransactionMapper;
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
        return (List<Transaction>) repository.findAll(transaction.processingStatus.notIn(AUTO_PROCESSED, IGNORE, MANUALY_FIXED));
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
