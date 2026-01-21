package navrat.name.moneta2lezeni.service;

import static navrat.name.moneta2lezeni.model.ProcessingStatus.AUTO_PROCESSED;
import static navrat.name.moneta2lezeni.model.ProcessingStatus.IGNORE;
import static navrat.name.moneta2lezeni.model.ProcessingStatus.MANUALY_FIXED;
import static navrat.name.moneta2lezeni.model.QTransaction.transaction;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import navrat.name.moneta2lezeni.dto.TransactionDto;
import navrat.name.moneta2lezeni.mapper.TransactionMapper;
import navrat.name.moneta2lezeni.model.Transaction;
import navrat.name.moneta2lezeni.producer.KafkaTransactionProducer;
import navrat.name.moneta2lezeni.repository.TransactionRepository;

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

    public void sendManualTransaction(TransactionDto dto) {
        try {
            kafkaProducer.sendTransaction(dto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
